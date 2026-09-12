package com.example.detectdanger.security.dns;

import com.example.detectdanger.security.ssrf_detect.IpAddressValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DnsSecurityService {
    private final IpAddressValidator ipAddressValidator;

    public List<String> resolveAndCheck(String host) {

        List<String> evidence =
                new ArrayList<>();

        if (host == null || host.isBlank()) {
            return evidence;
        }

        if (ipAddressValidator.isIpAddress(host)) {
            evidence.addAll(
                    checkAddress(host)
            );

            return evidence;
        }

        try {

            InetAddress[] addresses =
                    InetAddress.getAllByName(host);

            /*
             * DNS trả về nhiều IP.
             *
             * Chỉ cần một IP nguy hiểm cũng phải
             * coi hostname là suspicious.
             */
            for (InetAddress address : addresses) {

                String ip =
                        address.getHostAddress();

                evidence.addAll(
                        checkAddress(ip)
                );
            }

        } catch (UnknownHostException e) {

            evidence.add(
                    "Unable to resolve hostname"
            );
        }

        return evidence;
    }

    private List<String> checkAddress(
            String ip
    ) {

        List<String> evidence =
                new ArrayList<>();

        if (ipAddressValidator.isLoopback(ip)) {
            evidence.add("DNS resolved to loopback address: " + ip);
        }
        if (ipAddressValidator.isLocalhost(ip)) {
            evidence.add("DNS resolved to localhost address: " + ip);
        }

        if (ipAddressValidator.isPrivateNetwork(ip)) {
            evidence.add("DNS resolved to private IP: " + ip);
        }

        if (ipAddressValidator.isLinkLocal(ip)) {
            evidence.add("DNS resolved to link-local IP: " + ip);
        }

        if (ipAddressValidator.isAnyLocalAddress(ip)) {
            evidence.add("DNS resolved to unspecified IP: " + ip);
        }

        return evidence;
    }
}
