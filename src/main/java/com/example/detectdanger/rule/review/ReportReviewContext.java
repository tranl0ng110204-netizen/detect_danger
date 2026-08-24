package com.example.detectdanger.rule.review;

import com.example.detectdanger.entity.Report;
import com.example.detectdanger.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ReportReviewContext {
    private final Report report;
    private final User user;
    private final int reporterReputation;
    private final boolean duplicateReport;
    private final boolean inputAlreadyVerified;
    private final boolean inputAlreadyBlacklisted;
}
