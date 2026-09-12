package com.example.detectdanger.config;

import com.example.detectdanger.entity.Enum.InputType;
import com.example.detectdanger.entity.Enum.RuleStatus;
import com.example.detectdanger.entity.Enum.RuleType;
import com.example.detectdanger.entity.Rule;
import com.example.detectdanger.repository.RuleRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final RuleRepository ruleRepository;
    @Override
    public void run(String... args) {
        List<Rule> sampleRules = List.of(
                // ================== RULES DÀNH CHO EMAIL ==================
                Rule.builder()
                        .ruleName("Email rác / Email dùng một lần")
                        .ruleCode("DISPOSABLE_EMAIL")
                        .ruleStatus(RuleStatus.ACTIVE)
                        .inputType(InputType.EMAIL)
                        .ruleValue("10minutemail.com,tempmail.com,guerrillamail.com,mailinator.com,yopmail.com,temp-mail.org,throwawaymail.com")
                        .weight(30)
                        .version("1.0")
                        .isActive(true)
                        .ruleType(RuleType.SET_LOOKUP)
                        .reason("Địa chỉ email thuộc dịch vụ thư điện tử tạm thời / rác")
                        .build(),
                Rule.builder()
                        .ruleName("Email mạo danh hỗ trợ ngân hàng/bảo mật")
                        .ruleCode("SUSPICIOUS_EMAIL_SPOOF")
                        .ruleStatus(RuleStatus.ACTIVE)
                        .inputType(InputType.EMAIL)
                        .ruleValue(".*(support|security|alert|admin|verify).*(vcb|mbbank|bidv|techcombank).*@(gmail|yahoo|hotmail)\\.com")
                        .weight(40)
                        .version("1.0")
                        .isActive(true)
                        .ruleType(RuleType.REGEX)
                        .reason("Email cá nhân giả mạo bộ phận hỗ trợ/bảo mật của ngân hàng")
                        .build(),
                // ================== RULES DÀNH CHO SỐ ĐIỆN THOẠI ==================
                Rule.builder()
                        .ruleName("Đầu số quốc tế lừa đảo nháy máy (Wangiri)")
                        .ruleCode("INTERNATIONAL_SCAM_PREFIX")
                        .ruleStatus(RuleStatus.ACTIVE)
                        .inputType(InputType.PHONE)
                        .ruleValue("+252,+247,+881,+882,+231,+232")
                        .weight(45)
                        .version("1.0")
                        .isActive(true)
                        .ruleType(RuleType.PREFIX_MATCH)
                        .reason("Số điện thoại mang đầu số quốc tế có lịch sử lừa đảo cước viễn thông")
                        .build(),
                Rule.builder()
                        .ruleName("Đầu số dịch vụ cước phí cao / tổng đài rác")
                        .ruleCode("HIGH_RATE_SERVICE_PREFIX")
                        .ruleStatus(RuleStatus.ACTIVE)
                        .inputType(InputType.PHONE)
                        .ruleValue("1900,024888,028888")
                        .weight(20)
                        .version("1.0")
                        .isActive(true)
                        .ruleType(RuleType.PREFIX_MATCH)
                        .reason("Đầu số tổng đài tự động hoặc dịch vụ thu cước cao")
                        .build(),
                // ================== RULES DÀNH CHO URL / WEBSITE ==================
                Rule.builder()
                        .ruleName("Tên miền có đuôi rẻ tiền rủi ro cao")
                        .ruleCode("HIGH_RISK_TLD")
                        .ruleStatus(RuleStatus.ACTIVE)
                        .inputType(InputType.URL)
                        .ruleValue(".*\\.(xyz|top|tk|ml|cf|gq|click|buzz|rest)(/.*)?$")
                        .weight(25)
                        .version("1.0")
                        .isActive(true)
                        .ruleType(RuleType.REGEX)
                        .reason("Trang web sử dụng đuôi tên miền chi phí thấp thường dùng trong các chiến dịch phishing")
                        .build(),
                Rule.builder()
                        .ruleName("URL dùng địa chỉ IP trực tiếp")
                        .ruleCode("IP_ADDRESS_URL")
                        .ruleStatus(RuleStatus.ACTIVE)
                        .inputType(InputType.URL)
                        .ruleValue("^(http[s]?://)?\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}(:\\d+)?(/.*)?$")
                        .weight(35)
                        .version("1.0")
                        .isActive(true)
                        .ruleType(RuleType.REGEX)
                        .reason("Trang web không có tên miền, truy cập trực tiếp bằng địa chỉ IP máy chủ")
                        .build(),
                Rule.builder()
                        .ruleName("URL giả mạo thương hiệu ngân hàng lớn")
                        .ruleCode("BANK_PHISHING_URL")
                        .ruleStatus(RuleStatus.ACTIVE)
                        .inputType(InputType.URL)
                        .ruleValue(".*(vietcombank|techcombank|mbbank|vpbank|acb).*(login|dangnhap|xacthuc).*")
                        .weight(50)
                        .version("1.0")
                        .isActive(true)
                        .ruleType(RuleType.REGEX)
                        .reason("Đường dẫn có dấu hiệu giả mạo trang đăng nhập/xác thực của ngân hàng")
                        .build(),
                // ================== RULES DÀNH CHO SSRF & DNS REBINDING ==================
                Rule.builder()
                        .ruleName("URL trỏ về IP nội bộ / Localhost / Cloud Metadata (SSRF)")
                        .ruleCode("SSRF_INTERNAL_IP")
                        .ruleStatus(RuleStatus.ACTIVE)
                        .inputType(InputType.URL)
                        // Bắt localhost, 127.*, 10.*, 192.168.*, 172.16-31.*, 169.254.* (AWS/GCP metadata), IPv6 [::1], [::]
                        .ruleValue("^(localhost|127\\.\\d+\\.\\d+\\.\\d+|10\\.\\d+\\.\\d+\\.\\d+|192\\.168\\.\\d+\\.\\d+|172\\.(1[6-9]|2\\d|3[0-1])\\.\\d+\\.\\d+|169\\.254\\.\\d+\\.\\d+|\\[::1\\]|\\[::\\])$")
                        .weight(50)
                        .version("1.0")
                        .isActive(true)
                        .ruleType(RuleType.REGEX)
                        .reason("URL điều hướng trực tiếp vào mạng nội bộ, máy chủ cục bộ hoặc dịch vụ Cloud Metadata")
                        .build(),
                Rule.builder()
                        .ruleName("URL che giấu địa chỉ IP dạng Hex, Octal hoặc Decimal (SSRF Bypass)")
                        .ruleCode("SSRF_OBFUSCATED_IP")
                        .ruleStatus(RuleStatus.ACTIVE)
                        .inputType(InputType.URL)
                        // Bắt IP dạng Hex (0x7f000001), Octal (0177.0.0.1), Decimal đơn (2130706433), rút gọn (127.1)
                        .ruleValue("^(0x[0-9a-fA-F.]+|0[0-7]+(\\.[0-7]+)*|\\d{8,10}|127\\.1)$")
                        .weight(45)
                        .version("1.0")
                        .isActive(true)
                        .ruleType(RuleType.REGEX)
                        .reason("URL cố tình mã hóa địa chỉ IP dạng Hex/Decimal/Octal nhằm vượt qua bộ lọc an ninh")
                        .build(),
                Rule.builder()
                        .ruleName("Tên miền Wildcard DNS trỏ về IP nội bộ (SSRF Bypass)")
                        .ruleCode("SSRF_WILDCARD_DNS")
                        .ruleStatus(RuleStatus.ACTIVE)
                        .inputType(InputType.URL)
                        // Các domain công cộng luôn phân giải về localhost/nội bộ: nip.io, sslip.io, localtest.me, lvh.me, vcap.me
                        .ruleValue(".*\\.(nip\\.io|sslip\\.io|localtest\\.me|lvh\\.me|vcap\\.me|fwh\\.is)$")
                        .weight(45)
                        .version("1.0")
                        .isActive(true)
                        .ruleType(RuleType.REGEX)
                        .reason("Tên miền sử dụng dịch vụ Wildcard DNS tự động trỏ ngược về localhost hoặc mạng nội bộ")
                        .build(),
                Rule.builder()
                        .ruleName("Dịch vụ khai thác lỗ hổng DNS Rebinding")
                        .ruleCode("DNS_REBINDING_SERVICE")
                        .ruleStatus(RuleStatus.ACTIVE)
                        .inputType(InputType.URL)
                        // Các dịch vụ tự động tạo payload DNS Rebinding nổi tiếng: rbndr.us, lock.cmpx.ch, 1u.ms
                        .ruleValue(".*\\.(rbndr\\.us|lock\\.cmpx\\.ch|rebind\\.it|1u\\.ms)$")
                        .weight(50)
                        .version("1.0")
                        .isActive(true)
                        .ruleType(RuleType.REGEX)
                        .reason("URL sử dụng dịch vụ chuyên dụng để tạo payload tấn công DNS Rebinding")
                        .build(),
                Rule.builder()
                        .ruleName("URL nhắm vào cổng nhạy cảm nội bộ (Docker, Redis, DB)")
                        .ruleCode("SSRF_INTERNAL_PORT")
                        .ruleStatus(RuleStatus.ACTIVE)
                        .inputType(InputType.URL)
                        // Bắt các cổng nội bộ nguy hiểm: Redis (6379), Docker API (2375), Elasticsearch (9200), Mongo (27017), Web admin (8080, 8443, 8888)
                        .ruleValue(".*:(2375|6379|9200|27017|8080|8443|8888|9000)(/.*)?$")
                        .weight(30)
                        .version("1.0")
                        .isActive(true)
                        .ruleType(RuleType.REGEX)
                        .reason("URL chứa cổng kết nối tới các dịch vụ quản trị cơ sở dữ liệu và hạ tầng nội bộ")
                        .build()
        );
        int newRulesCount = 0;
        for (Rule rule : sampleRules) {
            if (!ruleRepository.existsByRuleCode(rule.getRuleCode())) {
                ruleRepository.save(rule);
                newRulesCount++;
                System.out.println(">>> Đã nạp thêm rule mới: " + rule.getRuleCode());
            }
        }
        if (newRulesCount > 0) {
            System.out.println(">>> Hoàn tất nạp " + newRulesCount + " rule mới vào Database!");
        } else {
            System.out.println(">>> Tất cả các rule đã tồn tại trong DB, không có rule mới nào cần nạp.");
        }

    }
}
