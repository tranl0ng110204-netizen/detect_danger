package com.example.detectdanger.rule.scan.url;

import com.example.detectdanger.entity.Enum.InputType;
import com.example.detectdanger.rule.scan.DetectionRule;
import com.example.detectdanger.rule.scan.RuleResult;
import com.example.detectdanger.rule.scan.RuleStatus;
import com.example.detectdanger.security.dns.DnsSecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DnsRebiddingRule implements DetectionRule {
    private final DnsSecurityService dnsSecurityService;

    private static final int WEIGHT = 40;
    @Override
    public String getCode() {
        return "DNS_REBINDING_DETECT";
    }

    @Override
    public String getName() {
        return "DNS Rebinding Detection";
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
        return "1.0";
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

        if (input == null || input.isBlank()) {

            return new RuleResult(
                    getCode(),
                    false,
                    0,
                    "URL is empty",
                    List.of()
            );
        }

        try {

            URI uri = new URI(input);

            String host = uri.getHost();

            if (host == null || host.isBlank()) {

                return new RuleResult(
                        getCode(),
                        true,
                        WEIGHT,
                        "Unable to validate DNS hostname",
                        List.of(
                                "Missing or invalid hostname"
                        )
                );
            }

            /*
             * Chỉ hostname mới cần DNS resolution.
             */
            List<String> evidence =
                    dnsSecurityService
                            .resolveAndCheck(host);

            if (evidence.isEmpty()) {

                return new RuleResult(
                        getCode(),
                        false,
                        0,
                        "No DNS-related suspicious pattern detected",
                        List.of()
                );
            }

            return new RuleResult(
                    getCode(),
                    true,
                    WEIGHT,
                    "Potential DNS rebinding-related risk detected",
                    evidence
            );

        } catch (URISyntaxException e) {

            return new RuleResult(
                    getCode(),
                    true,
                    WEIGHT,
                    "Invalid URL prevents DNS validation",
                    List.of(
                            "Invalid URL syntax"
                    )
            );
        }
    }


}
