package com.example.detectdanger.rule.blacklist;
import com.example.detectdanger.repository.BlackListRepository;
import com.example.detectdanger.entity.InputType;
import com.example.detectdanger.repository.VerifiedDataRepository;
import com.example.detectdanger.rule.DetectionRule;
import com.example.detectdanger.rule.RuleResult;
import com.example.detectdanger.rule.RuleStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BlacklistedDataRule implements DetectionRule {
    private static final int WEIGHT = 40;

    private final VerifiedDataRepository verifiedDataRepository;
    private final BlackListRepository blackListRepository;

    @Override
    public String getCode() {
        return "BLACKLISTED_DATA";
    }

    @Override
    public String getName() {
        return "Verified Blacklist Detection";
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

    /**
     * Blacklist có thể áp dụng cho tất cả loại input:
     * URL, EMAIL, PHONE, MESSAGE.
     */
    @Override
    public boolean supports(InputType inputType) {
        return true;
    }

    @Override
    public RuleResult evaluate(
            String input,
            InputType inputType

    ) {

        boolean matched = blackListRepository.
                existsByInputTypeAndNormalizedValueAndActiveTrue(
                    inputType,
                    input
        );
        if (matched) {

            return new RuleResult(
                    getCode(),
                    true,
                    WEIGHT,
                    "Input exists in verified blacklist",
                    List.of("VERIFIED_BLACKLIST")
            );
        }

        return new RuleResult(
                getCode(),
                false,
                0,
                "Input not found in verified blacklist",
                List.of()
        );
    }

}
