package com.example.detectdanger.rule.scan.email;

import com.example.detectdanger.entity.Enum.InputType;
import com.example.detectdanger.rule.scan.DetectionRule;
import com.example.detectdanger.rule.scan.RuleResult;
import com.example.detectdanger.rule.scan.RuleStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class DisposableEmailRule implements DetectionRule {
    private static final int WEIGHT = 20;

    /*
     * Danh sách domain email tạm thời.
     *
     * Đây là danh sách mẫu cho project.
     * Sau này có thể chuyển sang database.
     */
    private static final Set<String> DISPOSABLE_DOMAINS = Set.of(
            "10minutemail.com",
            "tempmail.com",
            "guerrillamail.com",
            "mailinator.com",
            "yopmail.com",
            "temp-mail.org",
            "throwawaymail.com"
    );

    @Override
    public String getCode() {
        return "DISPOSABLE_EMAIL";
    }

    @Override
    public String getName() {
        return "Disposable Email Detection";
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
        return inputType == InputType.EMAIL;
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
                    "Email is empty",
                    List.of()
            );
        }

        String email =
                input.trim().toLowerCase();

        /*
         * Email phải có @
         */
        int atIndex = email.lastIndexOf("@");

        if (atIndex <= 0
                || atIndex == email.length() - 1) {

            return new RuleResult(
                    getCode(),
                    false,
                    0,
                    "Invalid email format",
                    List.of()
            );
        }

        String domain =
                email.substring(atIndex + 1);

        if (DISPOSABLE_DOMAINS.contains(domain)) {

            return new RuleResult(
                    getCode(),
                    true,
                    WEIGHT,
                    "Disposable email domain detected",
                    List.of(
                            "Disposable email domain: "
                                    + domain
                    )
            );
        }

        return new RuleResult(
                getCode(),
                false,
                0,
                "No disposable email domain detected",
                List.of()
        );
    }
}
