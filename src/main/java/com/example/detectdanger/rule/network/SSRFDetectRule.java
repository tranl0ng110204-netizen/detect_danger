package com.example.detectdanger.rule.network;

import com.example.detectdanger.entity.InputType;
import com.example.detectdanger.rule.DetectionRule;
import com.example.detectdanger.rule.RuleResult;
import com.example.detectdanger.rule.RuleStatus;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Component
public class SSRFDetectRule implements DetectionRule {
    private static final int WEIGHT = 80;

    @Override
    public String getCode() {
        return "SSRF_DETECT";
    }

    @Override
    public String getName() {
        return "SSRF Detection";
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

        List<String> evidence = new ArrayList<>();

        if (!supports(inputType)) {
            return new RuleResult(
                    getCode(),
                    false,
                    0,
                    "Rule does not support this input type",
                    List.of()
            );
        }

        try {

            URI uri = new URI(input);

            String host = uri.getHost();

            if (host == null || host.isBlank()) {

                evidence.add("URL does not contain a valid host");

                return new RuleResult(
                        getCode(),
                        true,
                        WEIGHT,
                        "Suspicious URL detected",
                        evidence
                );
            }

            host = removeBrackets(host);

            // localhost
            if (isLocalhost(host)) {
                evidence.add(
                        "URL points to localhost"
                );
            }

            // IP address
            if (isIpAddress(host)) {

                if (isPrivateOrLocalIp(host)) {

                    evidence.add(
                            "URL points to a private or local IP address"
                    );
                }

                if (isCloudMetadataIp(host)) {

                    evidence.add(
                            "URL points to a cloud metadata endpoint"
                    );
                }
            }

        } catch (URISyntaxException e) {

            evidence.add(
                    "Invalid URL format"
            );
        }

        if (evidence.isEmpty()) {

            return new RuleResult(
                    getCode(),
                    false,
                    0,
                    "No SSRF pattern detected",
                    List.of()
            );
        }

        return new RuleResult(
                getCode(),
                true,
                WEIGHT,
                "Potential SSRF detected",
                evidence
        );
    }

    private boolean isLocalhost(String host) {

        String normalizedHost =
                host.toLowerCase();

        return normalizedHost.equals("localhost")
                || normalizedHost.equals("localhost.localdomain")
                || normalizedHost.equals("127.0.0.1")
                || normalizedHost.equals("0.0.0.0")
                || normalizedHost.equals("::1");
    }

    private boolean isIpAddress(String host) {

        // IPv4
        if (host.matches(
                "^\\d{1,3}(\\.\\d{1,3}){3}$"
        )) {
            return true;
        }

        // IPv6
        return host.contains(":");
    }

    private boolean isPrivateOrLocalIp(String host) {

        try {

            InetAddress address =
                    InetAddress.getByName(host);

            if (address.isLoopbackAddress()) {
                return true;
            }

            if (address.isSiteLocalAddress()) {
                return true;
            }

            if (address.isLinkLocalAddress()) {
                return true;
            }

            return isZeroAddress(host);

        } catch (Exception e) {

            return false;
        }
    }

    private boolean isCloudMetadataIp(String host) {

        return host.equals("169.254.169.254");
    }

    private boolean isZeroAddress(String host) {

        return host.equals("0.0.0.0")
                || host.equals("::");
    }

    private String removeBrackets(String host) {

        if (host.startsWith("[")
                && host.endsWith("]")) {

            return host.substring(
                    1,
                    host.length() - 1
            );
        }

        return host;
    }


}
