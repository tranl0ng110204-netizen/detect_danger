package com.example.detectdanger.rule.scan.url;

import com.example.detectdanger.entity.Enum.InputType;
import com.example.detectdanger.rule.scan.DetectionRule;
import com.example.detectdanger.rule.scan.RuleResult;
import com.example.detectdanger.rule.scan.RuleStatus;
import com.example.detectdanger.rule.scan.security.IpRangeChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SsrfDetectionRule implements DetectionRule {
    private final IpRangeChecker ipRangeChecker;

    private static final int WEIGHT = 45;

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
        return "1.6";
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

        List<String> evidence =
                new ArrayList<>();

        if (input == null || input.isBlank()) {

            return new RuleResult(
                    getCode(),
                    false,
                    0,
                    "URL is empty",
                    List.of()
            );
        }

        URI uri;
        try {
            uri = new URI(input.trim());
        } catch (URISyntaxException e) {
            return new RuleResult(
                    getCode(),
                    true,
                    WEIGHT,
                    "Invalid URL format",
                    List.of(
                            "URL cannot be parsed"
                    )
            );
        }


        /*
         * ============================================
         * SCHEME
         * ============================================
         */

        String scheme = uri.getScheme();
        if (scheme == null) {
            evidence.add("URL has no scheme");
        } else {
            String normalized = scheme.toLowerCase();
            if (!normalized.equals("http") && !normalized.equals("https")) {
                evidence.add("Unsupported URL scheme: " + scheme);
            }
        }

        /*
         * ============================================
         * HOST
         * ============================================
         */

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            evidence.add("URL does not contain a valid host");
        } else {
            host = normalizeHost(host);
            /*
             * ========================================
             * LOCALHOST
             * ========================================
             */
            if (isLocalhost(host)) {
                evidence.add("URL points to localhost");
            }
            /*
             * ========================================
             * DIRECT IP
             * ========================================
             */
            if (ipRangeChecker.isIpAddress(host)) {
                evidence.add("URL uses a direct IP address");
                /*
                 * CIDR
                 */
                if (ipRangeChecker.isPrivateOrReserved(host)) {
                    evidence.add(
                            "IP belongs to a private, "
                                    + "loopback, link-local "
                                    + "or reserved CIDR range"
                    );
                }
                /*
                 * Metadata
                 */
                if (ipRangeChecker
                        .isMetadataAddress(host)) {

                    evidence.add(
                            "URL points to a cloud "
                                    + "metadata endpoint"
                    );
                }
            }
        }


        /*
         * ============================================
         * USER INFO
         * ============================================
         */

        if (uri.getRawUserInfo() != null) {

            evidence.add(
                    "URL contains user information "
                            + "before the host"
            );
        }


        /*
         * ============================================
         * PORT
         * ============================================
         */

        int port =
                uri.getPort();

        if (port != -1
                && isSuspiciousPort(port)) {

            evidence.add(
                    "URL uses a potentially "
                            + "sensitive port: "
                            + port
            );
        }


        /*
         * ============================================
         * RESULT
         * ============================================
         */

        if (evidence.isEmpty()) {

            return new RuleResult(
                    getCode(),
                    false,
                    0,
                    "No SSRF-related URL pattern detected",
                    List.of()
            );
        }


        return new RuleResult(
                getCode(),
                true,
                WEIGHT,
                "Potential SSRF-related URL pattern detected",
                evidence
        );
    }


    /*
     * ================================================
     * NORMALIZE HOST
     * ================================================
     */

    private String normalizeHost(
            String host
    ) {
        String result = host.trim();
        /*
         * IPv6 URI:
         *
         * [::1]
         */
        if (result.startsWith("[") && result.endsWith("]")) {
            result = result.substring(1, result.length() - 1);
        }
        /*
         * FQDN:
         *
         * example.com.
         */
        if (result.endsWith(".")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    /*
     * ================================================
     * LOCALHOST
     * ================================================
     */

    private boolean isLocalhost(
            String host
    ) {
        return host.equalsIgnoreCase(
                "localhost"
        )
                || host.equalsIgnoreCase(
                "localhost.localdomain"
        )
                || host.equals("127.0.0.1")
                || host.equals("::1");
    }


    /*
     * ================================================
     * SUSPICIOUS PORT
     * ================================================
     */

    private boolean isSuspiciousPort(
            int port
    ) {

        return port == 22
                || port == 23
                || port == 25
                || port == 53
                || port == 110
                || port == 135
                || port == 139
                || port == 445
                || port == 1433
                || port == 1521
                || port == 3306
                || port == 5432
                || port == 6379
                || port == 8080
                || port == 8443;
    }
}
