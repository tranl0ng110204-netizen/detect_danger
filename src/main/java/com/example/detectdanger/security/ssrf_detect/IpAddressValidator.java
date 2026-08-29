package com.example.detectdanger.security.ssrf_detect;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class IpAddressValidator {
    private final InetAddressValidator validator = InetAddressValidator.getInstance();
    // ===== check xem co phai la IPv4 hay khong =======
    public boolean isIPv4(String host) {
        return host != null
                && validator.isValidInet4Address(host);
    }

    // ===== check xem co phai la IPv6 hay khong =======
    public boolean isIPv6(String host) {
        return host != null
                && validator.isValidInet6Address(host);
    }

    // ===== check xem co phai la IP chuan hay khong =======
    public boolean isIpAddress(String host) {
        return isIPv4(host) || isIPv6(host);
    }

    // ===== check xem co phai la localhost hay khong =======
    public boolean isLocalhost(String host) {

        if (host == null || host.isBlank()) {
            return false;
        }

        String normalized = host;

        return normalized.equals("localhost")
                || normalized.equals("localhost.localdomain")
                || normalized.equals("127.0.0.1")
                || normalized.equals("::1");
    }

    /**
     * Kiểm tra loopback address.
     * IPv4:
     * 127.0.0.0/8
     * IPv6:
     * ::1
     */
    public boolean isLoopback(String host) {
        InetAddress address = resolveLiteral(host);
        if (address == null) {
            return false;
        }
        return address.isLoopbackAddress();
    }

    /**
     * Kiểm tra private network.

     * IPv4:
     * 10.0.0.0/8
     * 172.16.0.0/12
     * 192.168.0.0/16
     * Java isSiteLocalAddress() hỗ trợ các private IPv4 range trên.
     */
    public boolean isPrivateNetwork(String host) {

        InetAddress address = resolveLiteral(host);

        if (address == null) {
            return false;
        }

        return address.isSiteLocalAddress();
    }

    /**
     * Kiểm tra link-local.
     *
     * IPv4:
     * 169.254.0.0/16
     *
     * IPv6:
     * fe80::/10
     */
    public boolean isLinkLocal(String host) {

        InetAddress address = resolveLiteral(host);

        if (address == null) {
            return false;
        }

        return address.isLinkLocalAddress();
    }

    /**
     * Kiểm tra wildcard / unspecified address.
     *
     * IPv4:
     * 0.0.0.0
     *
     * IPv6:
     * ::
     */
    public boolean isAnyLocalAddress(String host) {

        InetAddress address = resolveLiteral(host);

        if (address == null) {
            return false;
        }

        return address.isAnyLocalAddress();
    }

    /**
     * Kiểm tra IP literal mà không cố tình resolve hostname.
     */
    private InetAddress resolveLiteral(String host) {

        if (host == null || host.isBlank()) {
            return null;
        }
        if (!isIPv4(host) && !isIPv6(host)) {
            return null;
        }
        try {
            return InetAddress.getByName(host);

        } catch (UnknownHostException e) {

            return null;
        }
    }



}
