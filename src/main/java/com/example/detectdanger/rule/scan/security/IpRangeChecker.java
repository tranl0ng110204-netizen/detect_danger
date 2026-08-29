package com.example.detectdanger.rule.scan.security;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.List;

@Component
public class IpRangeChecker {
    private final InetAddressValidator validator =
            InetAddressValidator.getInstance();

    private static final List<String> IPV4_BLOCKED_RANGES = List.of(

            // Loopback
            "127.0.0.0/8",

            // Private networks
            "10.0.0.0/8",
            "172.16.0.0/12",
            "192.168.0.0/16",

            // Link-local
            "169.254.0.0/16",

            // Unspecified / special
            "0.0.0.0/8",

            // Shared address space
            "100.64.0.0/10",

            // Benchmark / testing
            "198.18.0.0/15"
    );

    private static final List<String> IPV6_BLOCKED_RANGES = List.of(

            // Unspecified
            "::/128",

            // Loopback
            "::1/128",

            // Unique Local Address
            "fc00::/7",

            // Link-local
            "fe80::/10"
    );

    /*
     * Cloud metadata endpoint.
     */
    private static final String METADATA_IP =
            "169.254.169.254";

    public boolean isIPv4(String host) {

        return host != null
                && validator.isValidInet4Address(host);
    }


    public boolean isIPv6(String host) {

        return host != null
                && validator.isValidInet6Address(host);
    }

    public boolean isIpAddress(String host) {

        return isIPv4(host)
                || isIPv6(host);
    }

    public boolean isPrivateOrReserved(String ip) {

        if (!isIpAddress(ip)) {
            return false;
        }

        List<String> ranges =
                isIPv4(ip)
                        ? IPV4_BLOCKED_RANGES
                        : IPV6_BLOCKED_RANGES;

        for (String cidr : ranges) {
            if (isInCidr(ip, cidr)) {
                return true;
            }
        }
        return false;
    }

    public boolean isMetadataAddress(String ip) {

        return METADATA_IP.equals(ip);
    }


    /**
     * CIDR matching.
     *
     * Ví dụ:
     *
     * 10.1.2.3
     *
     * nằm trong:
     *
     * 10.0.0.0/8
     */
    public boolean isInCidr(
            String ip,
            String cidr
    ) {

        try {

            InetAddress ipAddress =
                    InetAddress.getByAddress(
                            parseIp(ip)
                    );

            CidrNetwork network =
                    parseCidr(cidr);

            byte[] ipBytes =
                    ipAddress.getAddress();

            byte[] networkBytes =
                    network.networkAddress();

            if (ipBytes.length != networkBytes.length) {
                return false;
            }

            int prefixLength =
                    network.prefixLength();

            int fullBytes =
                    prefixLength / 8;

            int remainingBits =
                    prefixLength % 8;

            /*
             * Compare complete bytes.
             */
            for (int i = 0; i < fullBytes; i++) {

                if (ipBytes[i] != networkBytes[i]) {

                    return false;
                }
            }

            /*
             * Compare remaining bits.
             */
            if (remainingBits > 0) {

                int mask =
                        0xFF << (8 - remainingBits);

                int ipPart =
                        ipBytes[fullBytes] & mask;

                int networkPart =
                        networkBytes[fullBytes] & mask;

                if (ipPart != networkPart) {

                    return false;
                }
            }

            return true;

        } catch (Exception e) {

            return false;
        }
    }

    private byte[] parseIp(String ip) {

        if (isIPv4(ip)) {

            String[] parts =
                    ip.split("\\.");

            byte[] bytes =
                    new byte[4];
            for (int i = 0; i < 4; i++) {
                bytes[i] =
                        (byte) Integer.parseInt(parts[i]);
            }

            return bytes;
        }

        if (isIPv6(ip)) {

            return parseIPv6(ip);
        }

        throw new IllegalArgumentException(
                "Invalid IP address"
        );
    }
    private byte[] parseIPv6(String ip) {

        String normalized =
                ip.toLowerCase();

        /*
         * IPv6 có ::
         */
        if (normalized.contains("::")) {

            return parseCompressedIPv6(normalized);
        }

        return parseFullIPv6(normalized);
    }

    private byte[] parseFullIPv6(
            String ip
    ) {

        String[] groups =
                ip.split(":", -1);

        if (groups.length != 8) {

            throw new IllegalArgumentException(
                    "Invalid IPv6 address"
            );
        }

        byte[] result =
                new byte[16];

        for (int i = 0; i < 8; i++) {

            int value =
                    Integer.parseInt(
                            groups[i],
                            16
                    );

            result[i * 2] =
                    (byte) ((value >> 8) & 0xff);

            result[i * 2 + 1] =
                    (byte) (value & 0xff);
        }

        return result;
    }


    private byte[] parseCompressedIPv6(
            String ip
    ) {

        String[] sides =
                ip.split("::", -1);

        if (sides.length != 2) {

            throw new IllegalArgumentException(
                    "Invalid IPv6 compression"
            );
        }

        String left =
                sides[0];

        String right =
                sides[1];

        String[] leftGroups =
                left.isEmpty()
                        ? new String[0]
                        : left.split(":");

        String[] rightGroups =
                right.isEmpty()
                        ? new String[0]
                        : right.split(":");

        int totalGroups =
                leftGroups.length
                        + rightGroups.length;

        if (totalGroups >= 8) {

            throw new IllegalArgumentException(
                    "Invalid IPv6 compression"
            );
        }

        int zeroGroups =
                8 - totalGroups;

        byte[] result =
                new byte[16];

        int index = 0;

        /*
         * Left side.
         */
        for (String group : leftGroups) {

            index =
                    writeIPv6Group(
                            result,
                            index,
                            group
                    );
        }

        /*
         * Compressed zero groups.
         */
        for (int i = 0;
             i < zeroGroups;
             i++) {

            result[index++] = 0;
            result[index++] = 0;
        }

        /*
         * Right side.
         */
        for (String group : rightGroups) {

            index =
                    writeIPv6Group(
                            result,
                            index,
                            group
                    );
        }

        return result;
    }


    private int writeIPv6Group(
            byte[] result,
            int index,
            String group
    ) {

        if (group.length() < 1
                || group.length() > 4) {

            throw new IllegalArgumentException(
                    "Invalid IPv6 group"
            );
        }

        int value =
                Integer.parseInt(
                        group,
                        16
                );

        result[index++] =
                (byte) ((value >> 8) & 0xff);

        result[index++] =
                (byte) (value & 0xff);

        return index;
    }


    private CidrNetwork parseCidr(
            String cidr
    ) {

        String[] parts =
                cidr.split("/");

        if (parts.length != 2) {

            throw new IllegalArgumentException(
                    "Invalid CIDR"
            );
        }

        String networkIp =
                parts[0];

        int prefixLength =
                Integer.parseInt(parts[1]);

        byte[] networkBytes =
                parseIp(networkIp);

        int maxBits =
                networkBytes.length * 8;

        if (prefixLength < 0
                || prefixLength > maxBits) {

            throw new IllegalArgumentException(
                    "Invalid CIDR prefix"
            );
        }

        /*
         * Normalize network address.
         *
         * Ví dụ:
         *
         * 10.123.50.20/8
         *
         * trở thành:
         *
         * 10.0.0.0
         */
        byte[] normalizedNetwork =
                networkBytes.clone();

        int fullBytes =
                prefixLength / 8;

        int remainingBits =
                prefixLength % 8;

        if (remainingBits > 0
                && fullBytes < normalizedNetwork.length) {

            int mask =
                    0xFF << (8 - remainingBits);

            normalizedNetwork[fullBytes] =
                    (byte)
                            (normalizedNetwork[fullBytes]
                                    & mask);

            fullBytes++;
        }

        for (int i = fullBytes;
             i < normalizedNetwork.length;
             i++) {

            normalizedNetwork[i] = 0;
        }

        return new CidrNetwork(
                normalizedNetwork,
                prefixLength
        );
    }


    private record CidrNetwork(
            byte[] networkAddress,
            int prefixLength
    ) {
    }
}
