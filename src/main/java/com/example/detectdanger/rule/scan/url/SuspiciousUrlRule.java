package com.example.detectdanger.rule.scan.url;

import com.example.detectdanger.entity.Enum.InputType;
import com.example.detectdanger.rule.scan.DetectionRule;
import com.example.detectdanger.rule.scan.RuleResult;
import com.example.detectdanger.rule.scan.RuleStatus;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Component
public class SuspiciousUrlRule implements DetectionRule {
    private static final int WEIGHT = 25;

    @Override
    public String getCode() {
        return "SUSPICIOUS_URL";
    }

    @Override
    public String getName() {
        return "Suspicious URL Detection";
    }

    @Override
    public int getWeight() {
        return WEIGHT;
    }

    @Override
    public RuleStatus getStatus() {
        return RuleStatus.ACTIVE;
    }

    @Override
    public String getVersion() {
        return "1.1";
    }

    @Override
    public boolean supports(InputType inputType) {
        return inputType == InputType.URL;
    }



    @Override
    public RuleResult evaluate(
            String input,
            InputType inputType

    ) {

        List<String> evidence = new ArrayList<>();

        try {

            URI uri = new URI(input);

            String host = uri.getHost();

            if (host == null) {
                evidence.add(
                        "Missing hostname"
                );

            } else {
                if (host.split("\\.").length > 4) {
                    evidence.add(
                            "Unusually high number of subdomains"
                    );
                }
                if (host.contains("--")) {
                    evidence.add(
                            "Hostname contains suspicious pattern"
                    );
                }
            }
            if (uri.getUserInfo() != null) {
                evidence.add(
                        "URL contains user information"
                );
            }
            String path =
                    uri.getPath();
            if (path != null
                    && path.length() > 200) {
                evidence.add(
                        "Unusually long URL path"
                );
            }
        } catch (URISyntaxException e) {

            evidence.add("Invalid URL format");
        }

        if (evidence.isEmpty()) {

            return new RuleResult(
                    getCode(),
                    false,
                    0,
                    "No suspicious URL pattern detected",
                    List.of()
            );
        }

        return new RuleResult(
                getCode(),
                true,
                WEIGHT,
                "Suspicious URL pattern detected",
                evidence
        );
    }

    private boolean isIpAddress(String host) {

        return host.matches(
                "^\\d{1,3}(\\.\\d{1,3}){3}$"
        );
    }

}
