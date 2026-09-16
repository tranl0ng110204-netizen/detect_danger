package com.example.detectdanger.rule.scan;

import com.example.detectdanger.entity.Enum.InputType;
import com.example.detectdanger.entity.Enum.RuleStatus;
import com.example.detectdanger.entity.Rule;
import com.example.detectdanger.repository.RuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DynamicRuleEngine {

    private final RuleRepository ruleRepository;

    // Cache compiled Regex Pattern để tránh compile lại mỗi request
    private final Map<String, Pattern> patternCache = new ConcurrentHashMap<>();

    /**
     * Snapshot chứa version string + danh sách rule đã load.
     * Dùng để gộp 2 query (getEngineVersion + evaluate) thành 1 lần load từ DB.
     */
    public record EngineSnapshot(String version, List<Rule> rules) {}

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Load rules từ DB 1 lần duy nhất, trả về snapshot để dùng cho cả
     * kiểm tra version lẫn evaluate — tránh 2 DB round-trips.
     */
    @Transactional(readOnly = true)
    public EngineSnapshot loadSnapshot(InputType inputType) {
        List<Rule> rules = ruleRepository.findByInputTypeAndRuleStatus(inputType, RuleStatus.ACTIVE);
        String version = buildVersion(rules);
        return new EngineSnapshot(version, rules);
    }

    /**
     * Chỉ lấy version (dùng khi không cần evaluate, vd: getScanById cache check).
     */
    @Transactional(readOnly = true)
    public String getEngineVersion(InputType inputType) {
        List<Rule> rules = ruleRepository.findByInputTypeAndRuleStatus(inputType, RuleStatus.ACTIVE);
        return buildVersion(rules);
    }

    /**
     * Evaluate dựa trên snapshot đã load sẵn — không query DB thêm.
     */
    public List<RuleResult> evaluate(String input, EngineSnapshot snapshot) {
        String cleanInput = input.trim().toLowerCase();
        return snapshot.rules().stream()
                .map(rule -> matchRule(rule, cleanInput, detectInputType(snapshot.rules())))
                .collect(Collectors.toList());
    }

    /**
     * Evaluate trực tiếp từ inputType — load rules từ DB.
     * Dùng cho các nơi chỉ cần evaluate mà không cần version.
     */
    @Transactional(readOnly = true)
    public List<RuleResult> evaluate(String input, InputType inputType) {
        List<Rule> rules = ruleRepository.findByInputTypeAndRuleStatus(inputType, RuleStatus.ACTIVE);
        String cleanInput = input.trim().toLowerCase();
        return rules.stream()
                .map(rule -> matchRule(rule, cleanInput, inputType))
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private String buildVersion(List<Rule> rules) {
        return rules.stream()
                .map(r -> r.getRuleCode() + ":" + r.getVersion())
                .sorted()
                .collect(Collectors.joining("|"));
    }

    /**
     * Lấy InputType từ danh sách rules (tất cả rules trong snapshot cùng InputType).
     */
    private InputType detectInputType(List<Rule> rules) {
        return rules.isEmpty() ? InputType.URL : rules.get(0).getInputType();
    }

    private RuleResult matchRule(Rule rule, String input, InputType inputType) {
        boolean matched = false;
        String matchEvidence = "";

        String hostOnly = (inputType == InputType.URL) ? extractHost(input) : input;

        switch (rule.getRuleType()) {
            case SET_LOOKUP -> {
                String domain = extractDomain(input, inputType);
                Set<String> blackListSet = Arrays.stream(rule.getRuleValue().split(","))
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .collect(Collectors.toSet());

                if (blackListSet.contains(domain) || blackListSet.contains(input)) {
                    matched = true;
                    matchEvidence = "Phát hiện giá trị trong danh sách đen: " + (domain.isEmpty() ? input : domain);
                }
            }
            case PREFIX_MATCH -> {
                String[] prefixes = rule.getRuleValue().split(",");
                for (String prefix : prefixes) {
                    if (input.startsWith(prefix.trim())) {
                        matched = true;
                        matchEvidence = "Phát hiện tiền tố rủi ro: " + prefix.trim();

                    }
                }
            }
            case KEYWORD_CONTAINS -> {
                String[] keyWords = rule.getRuleValue().split(",");
                for (String word : keyWords) {
                    String kw = word.trim().toLowerCase();
                    if (input.contains(kw)) {
                        matched = true;
                        matchEvidence = "Phát hiện từ khóa đáng ngờ: " + kw;

                    }
                }
            }
            case REGEX -> {
                try {
                    Pattern pattern = patternCache.computeIfAbsent(
                            rule.getRuleValue(),
                            val -> Pattern.compile(val, Pattern.CASE_INSENSITIVE)
                    );
                    if (pattern.matcher(hostOnly).find()) {
                        matched = true;
                        matchEvidence = "Phát hiện dấu hiệu rủi ro trên Host/Domain: " + hostOnly;
                    } else if (pattern.matcher(input).find()) {
                        matched = true;
                        matchEvidence = "Phát hiện dấu hiệu rủi ro trên nội dung";
                    }
                } catch (Exception e) {
                    // Tránh crash nếu admin nhập regex lỗi
                }
            }
        }

        return new RuleResult(
                rule.getRuleCode(),
                matched,
                matched ? rule.getWeight() : 0,
                matched ? rule.getReason() : "",
                matched ? List.of(matchEvidence) : List.of("không tìm thấy dấu hiệu nguy hiểm")
        );
    }

    private String extractDomain(String input, InputType inputType) {
        if (inputType == InputType.EMAIL && input.contains("@")) {
            return input.substring(input.lastIndexOf("@") + 1);
        }
        if (inputType == InputType.URL) {
            String domain = input.replaceFirst("^(http[s]?://)?(www\\.)?", "");
            int slashIndex = domain.indexOf('/');
            return slashIndex > 0 ? domain.substring(0, slashIndex) : domain;
        }
        return "";
    }

    private String extractHost(String url) {
        try {
            String tempUrl = url;
            if (!tempUrl.startsWith("http://") && !tempUrl.startsWith("https://")) {
                tempUrl = "http://" + tempUrl;
            }
            URI uri = new URI(tempUrl);
            String host = uri.getHost();
            return (host != null) ? host : url;
        } catch (Exception e) {
            String clean = url.replaceFirst("^(http[s]?://)?(www\\.)?", "");
            int slashIndex = clean.indexOf('/');
            int colonIndex = clean.indexOf(':');
            int endIndex = clean.length();
            if (slashIndex != -1) endIndex = slashIndex;
            if (colonIndex != -1 && colonIndex < endIndex) endIndex = colonIndex;
            return clean.substring(0, endIndex);
        }
    }
}
