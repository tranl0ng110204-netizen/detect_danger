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
                        .build(),
                // ================== RULES DÀNH CHO TIN NHẮN (SMS / CHAT) ==================
                Rule.builder()
                        .ruleName("Tin nhắn trúng thưởng / quà tặng bất ngờ")
                        .ruleCode("MSG_LOTTERY_SCAM")
                        .ruleStatus(RuleStatus.ACTIVE)
                        .inputType(InputType.MESSAGE)
                        .ruleValue("(?i).*(trúng thưởng|trung thuong|nhận quà|nhan qua|quà tặng|phan thuong|phần thưởng|giải thưởng|giftcode|voucher miễn phí).*")
                        .weight(40)
                        .version("1.0")
                        .isActive(true)
                        .ruleType(RuleType.REGEX)
                        .reason("Tin nhắn thông báo trúng thưởng / quà tặng bất ngờ - dấu hiệu lừa đảo phổ biến")
                        .build(),
                Rule.builder()
                        .ruleName("Tin nhắn yêu cầu cung cấp OTP / mã xác thực")
                        .ruleCode("MSG_OTP_REQUEST")
                        .ruleStatus(RuleStatus.ACTIVE)
                        .inputType(InputType.MESSAGE)
                        .ruleValue("(?i).*(cung cấp|đọc|gửi|nhập|share|give).{0,20}(otp|mã xác thực|ma xac thuc|mã otp|verification code|security code).*")
                        .weight(50)
                        .version("1.0")
                        .isActive(true)
                        .ruleType(RuleType.REGEX)
                        .reason("Tin nhắn yêu cầu cung cấp OTP - hành vi lừa đảo chiếm đoạt tài khoản điển hình")
                        .build(),
                Rule.builder()
                        .ruleName("Tin nhắn giả danh ngân hàng / cơ quan chức năng")
                        .ruleCode("MSG_BANK_IMPERSONATION")
                        .ruleStatus(RuleStatus.ACTIVE)
                        .inputType(InputType.MESSAGE)
                        .ruleValue("(?i).*(vietcombank|techcombank|mbbank|bidv|vpbank|acb|agribank|sacombank|công an|canh sat|tòa án|viện kiểm sát|thuế|hải quan).*(khóa|khoa|tạm giữ|phong tỏa|phong toa|xác minh|xac minh|vi phạm|điều tra).*")
                        .weight(50)
                        .version("1.0")
                        .isActive(true)
                        .ruleType(RuleType.REGEX)
                        .reason("Tin nhắn giả danh ngân hàng / cơ quan chức năng để đe dọa và yêu cầu cung cấp thông tin")
                        .build(),
                Rule.builder()
                        .ruleName("Tin nhắn kêu gọi đầu tư / tiền ảo lợi nhuận cao")
                        .ruleCode("MSG_CRYPTO_INVEST_SCAM")
                        .ruleStatus(RuleStatus.ACTIVE)
                        .inputType(InputType.MESSAGE)
                        .ruleValue("(?i).*(đầu tư|dau tu|sinh lời|sinh loi|lợi nhuận|loi nhuan).{0,30}(usdt|bitcoin|btc|eth|crypto|forex|chứng khoán|chung khoan).{0,30}(cam kết|cam ket|\\d+\\s*%/|\\d+\\s*%/tháng|không rủi ro|khong rui ro).*")
                        .weight(45)
                        .version("1.0")
                        .isActive(true)
                        .ruleType(RuleType.REGEX)
                        .reason("Tin nhắn kêu gọi đầu tư tài chính / tiền ảo với lợi nhuận cao bất thường")
                        .build(),
                Rule.builder()
                        .ruleName("Tin nhắn chứa URL rút gọn đáng ngờ")
                        .ruleCode("MSG_SHORTENED_URL")
                        .ruleStatus(RuleStatus.ACTIVE)
                        .inputType(InputType.MESSAGE)
                        .ruleValue("(?i).*(bit\\.ly|tinyurl\\.com|t\\.co|goo\\.gl|ow\\.ly|is\\.gd|buff\\.ly|shorturl\\.at|cutt\\.ly|rebrand\\.ly).*")
                        .weight(30)
                        .version("1.0")
                        .isActive(true)
                        .ruleType(RuleType.REGEX)
                        .reason("Tin nhắn chứa đường dẫn rút gọn - thường dùng để che giấu URL lừa đảo thật")
                        .build(),
                Rule.builder()
                        .ruleName("Tin nhắn thúc ép hành động khẩn cấp")
                        .ruleCode("MSG_URGENCY_PRESSURE")
                        .ruleStatus(RuleStatus.ACTIVE)
                        .inputType(InputType.MESSAGE)
                        .ruleValue("(?i).*(khẩn cấp|khan cap|ngay lập tức|ngay lap tuc|trong vòng \\d+ (phút|giờ|giờ)|hết hạn|het han|sắp bị khóa|sap bi khoa|24h|48h).*")
                        .weight(25)
                        .version("1.0")
                        .isActive(true)
                        .ruleType(RuleType.REGEX)
                        .reason("Tin nhắn sử dụng ngôn ngữ thúc ép khẩn cấp nhằm khiến nạn nhân mất bình tĩnh và làm theo yêu cầu")
                        .build(),
                Rule.builder()
                        .ruleName("Tin nhắn chứa số điện thoại / STK lạ kèm yêu cầu chuyển tiền")
                        .ruleCode("MSG_TRANSFER_REQUEST")
                        .ruleStatus(RuleStatus.ACTIVE)
                        .inputType(InputType.MESSAGE)
                        .ruleValue("(?i).*(chuyển (tiền|khoản)|chuyen (tien|khoan)|nộp tiền|nop tien|thanh toán|thanh toan).{0,50}(stk|số tài khoản|so tai khoan|\\d{9,16}).*")
                        .weight(40)
                        .version("1.0")
                        .isActive(true)
                        .ruleType(RuleType.REGEX)
                        .reason("Tin nhắn yêu cầu chuyển tiền kèm số tài khoản - dấu hiệu lừa đảo chuyển khoản")
                        .build(),

// ================== RULES DÀNH CHO TỆP TIN (FILE) ==================
                Rule.builder()
                        .ruleName("Tệp thực thi nguy hiểm (Executable)")
                        .ruleCode("FILE_EXECUTABLE")
                        .ruleStatus(RuleStatus.ACTIVE)
                        .inputType(InputType.FILE)
                        .ruleValue("(?i).*\\.(exe|scr|com|pif|bat|cmd|msi|msp|hta|cpl|jar|vbs|vbe|js|jse|wsf|wsh|ps1|psm1|reg|inf)$")
                        .weight(50)
                        .version("1.0")
                        .isActive(true)
                        .ruleType(RuleType.REGEX)
                        .reason("Tệp có phần mở rộng thực thi được - nguy cơ cao chứa mã độc hoặc script tấn công")
                        .build(),
                Rule.builder()
                        .ruleName("Tệp văn phòng có chứa Macro (Office Malware)")
                        .ruleCode("FILE_OFFICE_MACRO")
                        .ruleStatus(RuleStatus.ACTIVE)
                        .inputType(InputType.FILE)
                        .ruleValue("(?i).*\\.(docm|dotm|xlsm|xltm|xlam|pptm|potm|ppam|sldm)$")
                        .weight(45)
                        .version("1.0")
                        .isActive(true)
                        .ruleType(RuleType.REGEX)
                        .reason("Tệp Office hỗ trợ Macro - vector phổ biến để phát tán mã độc qua email/tin nhắn")
                        .build(),
                Rule.builder()
                        .ruleName("Tệp Android độc hại (APK)")
                        .ruleCode("FILE_ANDROID_APK")
                        .ruleStatus(RuleStatus.ACTIVE)
                        .inputType(InputType.FILE)
                        .ruleValue("(?i).*\\.(apk|aab|xapk|apks)$")
                        .weight(40)
                        .version("1.0")
                        .isActive(true)
                        .ruleType(RuleType.REGEX)
                        .reason("Tệp cài đặt ứng dụng Android - thường dùng để phát tán mã độc ngân hàng (banking trojan)")
                        .build(),
                Rule.builder()
                        .ruleName("Tệp nén có mật khẩu / đáng ngờ")
                        .ruleCode("FILE_SUSPICIOUS_ARCHIVE")
                        .ruleStatus(RuleStatus.ACTIVE)
                        .inputType(InputType.FILE)
                        .ruleValue("(?i).*\\.(rar|zip|7z|iso|img|vhd|vhdx|ace|cab|tar\\.gz|tgz)$")
                        .weight(20)
                        .version("1.0")
                        .isActive(true)
                        .ruleType(RuleType.REGEX)
                        .reason("Tệp nén - thường dùng để che giấu mã độc hoặc vượt qua bộ lọc email/AV đơn giản")
                        .build(),
                Rule.builder()
                        .ruleName("Tệp có đuôi kép (Double Extension) đánh lừa người dùng")
                        .ruleCode("FILE_DOUBLE_EXTENSION")
                        .ruleStatus(RuleStatus.ACTIVE)
                        .inputType(InputType.FILE)
                        .ruleValue("(?i).*\\.(pdf|doc|docx|xls|xlsx|jpg|jpeg|png|gif|txt|csv|mp3|mp4)\\.(exe|scr|bat|cmd|vbs|js|jar|msi|com|pif|hta)$")
                        .weight(55)
                        .version("1.0")
                        .isActive(true)
                        .ruleType(RuleType.REGEX)
                        .reason("Tệp sử dụng đuôi kép để ngụy trang thành tệp văn bản/hình ảnh an toàn nhưng thực chất là tệp thực thi")
                        .build(),
                Rule.builder()
                        .ruleName("Tệp script / mã nguồn có thể thực thi")
                        .ruleCode("FILE_SCRIPT")
                        .ruleStatus(RuleStatus.ACTIVE)
                        .inputType(InputType.FILE)
                        .ruleValue("(?i).*\\.(sh|bash|zsh|ksh|csh|py|pyw|pl|rb|php|asp|aspx|jsp|lua|tcl|scala)$")
                        .weight(35)
                        .version("1.0")
                        .isActive(true)
                        .ruleType(RuleType.REGEX)
                        .reason("Tệp script có thể thực thi trên hệ thống - nguy cơ chạy mã độc khi mở trên môi trường phù hợp")
                        .build(),
                Rule.builder()
                        .ruleName("Tệp chứa ký tự điều khiển / Unicode ẩn (RTLO Attack)")
                        .ruleCode("FILE_RTLO_ATTACK")
                        .ruleStatus(RuleStatus.ACTIVE)
                        .inputType(InputType.FILE)
                        // Bắt ký tự Right-to-Left Override (U+202E) và các ký tự zero-width hay dùng để ngụy trang tên file
                        .ruleValue(".*[\\u202E\\u202D\\u200B\\u200C\\u200D\\uFEFF].*")
                        .weight(50)
                        .version("1.0")
                        .isActive(true)
                        .ruleType(RuleType.REGEX)
                        .reason("Tên tệp chứa ký tự Unicode ẩn (RTLO, zero-width) để đảo ngược hoặc ngụy trang phần mở rộng thật")
                        .build(),
                Rule.builder()
                        .ruleName("Tệp mạo danh tài liệu quan trọng (hóa đơn, biên lai, hợp đồng)")
                        .ruleCode("FILE_FAKE_DOCUMENT")
                        .ruleStatus(RuleStatus.ACTIVE)
                        .inputType(InputType.FILE)
                        .ruleValue("(?i).*(hoadon|hóa đơn|invoice|bienlai|biên lai|hopdong|hợp đồng|contract|bienban|biên bản|saoke|sao kê|chuyenkhoan|chuyển khoản).*\\.(exe|scr|bat|cmd|vbs|js|jar|msi|com|pif|hta|docm|xlsm|pptm)$")
                        .weight(55)
                        .version("1.0")
                        .isActive(true)
                        .ruleType(RuleType.REGEX)
                        .reason("Tệp mạo danh tài liệu quan trọng nhưng có định dạng thực thi hoặc macro - dấu hiệu tấn công có chủ đích")
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
