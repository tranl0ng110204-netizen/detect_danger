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
public class DNSRebindingRule implements DetectionRule {
    private static final int WEIGHT = 75;

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

                return new RuleResult(
                        getCode(),
                        false,
                        0,
                        "No valid hostname found",
                        List.of()
                );
            }

            host = removeBrackets(host);

            /*
             * Nếu hostname là IP trực tiếp thì
             * SSRFDetectRule sẽ chịu trách nhiệm.
             *
             * DNS Rebinding Rule tập trung vào hostname
             * cần DNS resolution.
             */
            if (isIpAddress(host)) {

                return new RuleResult(
                        getCode(),
                        false,
                        0,
                        "Host is already an IP address",
                        List.of()
                );
            }

            InetAddress[] addresses =
                    InetAddress.getAllByName(host);

            if (addresses.length == 0) {

                return new RuleResult(
                        getCode(),
                        false,
                        0,
                        "Hostname could not be resolved",
                        List.of()
                );
            }

            for (InetAddress address : addresses) {

                if (isDangerousAddress(address)) {

                    evidence.add(
                            "Hostname resolves to a private, local or link-local address: "
                                    + address.getHostAddress()
                    );
                }
            }

        } catch (URISyntaxException e) {

            evidence.add(
                    "Invalid URL format"
            );

        } catch (Exception e) {

            /*
             * DNS resolution failure không nên tự động
             * coi là DNS rebinding.
             */
            return new RuleResult(
                    getCode(),
                    false,
                    0,
                    "DNS resolution could not be completed",
                    List.of()
            );
        }

        if (evidence.isEmpty()) {

            return new RuleResult(
                    getCode(),
                    false,
                    0,
                    "No DNS rebinding pattern detected",
                    List.of()
            );
        }

        return new RuleResult(
                getCode(),
                true,
                WEIGHT,
                "Potential DNS rebinding detected",
                evidence
        );
    }

    private boolean isDangerousAddress(
            InetAddress address
    ) {

        return address.isLoopbackAddress()
                || address.isSiteLocalAddress()
                || address.isLinkLocalAddress()
                || isCloudMetadataAddress(address)
                || isZeroAddress(address);
    }

    private boolean isCloudMetadataAddress(
            InetAddress address
    ) {

        return address.getHostAddress()
                .equals("169.254.169.254");
    }

    private boolean isZeroAddress(
            InetAddress address
    ) {

        String ip =
                address.getHostAddress();

        return ip.equals("0.0.0.0")
                || ip.equals("::");
    }

    private boolean isIpAddress(String host) {

        return host.matches(
                "^\\d{1,3}(\\.\\d{1,3}){3}$"
        ) || host.contains(":");
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
