package com.example.detectdanger.rule.scan;

import com.example.detectdanger.entity.Enum.InputType;
import com.example.detectdanger.entity.Enum.RuleStatus;
import com.example.detectdanger.entity.Rule;
import com.example.detectdanger.repository.RuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.lang.reflect.Array;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DynamicRuleEngine {
    private final RuleRepository ruleRepository;
    private final Map<String, Pattern> patternCache = new ConcurrentHashMap<>();

    //get rule version
    public String getEngineVersion(InputType inputType){
        List<Rule> rules = ruleRepository.findByInputTypeAndRuleStatus(inputType
        , RuleStatus.ACTIVE);
        return rules.stream()
                .map(r->r.getRuleCode()+":" + r.getVersion() +":" + r.getWeight())
                .sorted()
                .collect(Collectors.joining("|"));
    }

    //quet noi dung dua tren rule
    public List<RuleResult> evaluate(String input,InputType inputType ){
        List<Rule> activeRules = ruleRepository.findByInputTypeAndRuleStatus(inputType,RuleStatus.ACTIVE);
        List<RuleResult> results = new ArrayList<>();
        String cleanInput = input.trim().toLowerCase();

        for(Rule rule : activeRules){
            RuleResult result = matchRule(rule,cleanInput,inputType);
            results.add(result);
        }

        return results;
    }

    private RuleResult matchRule(Rule rule, String input, InputType inputType){
        boolean matched = false;
        String matchEvidence = "";

        String hostOnly = (inputType == InputType.URL) ? extractHost(input) : input;

        switch (rule.getRuleType()){
            // Ví dụ: kiểm tra domain email có nằm trong danh sách không
            case SET_LOOKUP -> {
                String domain = extractDomain(input,inputType);
                Set<String> blackListSet = Arrays.stream(rule.getRuleValue().split(","))
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .collect(Collectors.toSet());

                if (blackListSet.contains(domain) || blackListSet.contains(input)){
                    matched = true;
                    matchEvidence = "Phát hiện giá trị trong danh sách đen: " + (domain.isEmpty() ? input : domain);
                }
            }
            case PREFIX_MATCH -> {
                String[] prefixes = rule.getRuleValue().split(",");
                for(String prefix : prefixes){
                    if(input.startsWith(prefix)){
                        matched = true;
                        matchEvidence = "Phát hiện tiền tố rủi ro: " + prefix;
                        break;
                    }
                }
            }
            case KEYWORD_CONTAINS -> {
                String[] keyWords = rule.getRuleValue().split(",");
                for(String word: keyWords){
                    String kw = word.trim().toLowerCase();
                    if(input.contains(kw)){
                        matched = true;
                        matchEvidence =  "Phát hiện từ khóa đáng ngờ: " + kw;
                        break;
                    }
                }
            }
            case REGEX -> {
                try {
                    //  Lấy Pattern từ Cache (nếu chưa có thì compile và lưu vào cache)
                    Pattern pattern = patternCache.computeIfAbsent(
                            rule.getRuleValue(),
                            val -> Pattern.compile(val, Pattern.CASE_INSENSITIVE)
                    );
                    // 2. XỬ LÝ THÔNG MINH CHO URL:
                    // Thử match trên Host trước (cho các rule SSRF, Domain, IP).
                    // Nếu không match, thử match trên toàn bộ Full URL (cho các rule quét Path, Query).
                    if (pattern.matcher(hostOnly).find()) {
                        matched = true;
                        matchEvidence = "Phát hiện dấu hiệu rủi ro trên Host/Domain: " + hostOnly;
                    } else if (pattern.matcher(input).find()) {
                        matched = true;
                        matchEvidence = "Phát hiện dấu hiệu rủi ro trên đường dẫn URL";
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
                matched ? List.of(matchEvidence) : List.of()
        );

    }
    private String extractDomain(String input, InputType inputType){
        if(inputType == InputType.EMAIL && input.contains("@")){
            return input.substring(input.lastIndexOf("@")+1);
        }
        if (inputType == InputType.URL) {
            String domain = input.replaceFirst("^(http[s]?://)?(www\\.)?", "");
            int slashIndex = domain.indexOf('/');
            return slashIndex > 0 ? domain.substring(0, slashIndex) : domain;
        }
        return "";
    }

    private String extractHost(String url){
        try {
            String tempUrl = url;
            if (!tempUrl.startsWith("http://") && !tempUrl.startsWith("https://")) {
                tempUrl = "http://" + tempUrl;
            }
            URI uri = new URI(tempUrl);
            String host = uri.getHost();
            return (host != null) ? host : url;
        } catch (Exception e) {
            // Fallback nếu URL dị dạng không parse được qua URI
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
