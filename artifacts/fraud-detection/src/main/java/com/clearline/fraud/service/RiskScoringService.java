package com.clearline.fraud.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

@Service
public class RiskScoringService {
    public RiskResult score(
            BigDecimal amount,
            String location,
            String device,
            LocalDateTime transactionTime,
            int frequency) {
        int score = 0;
        List<String> reasons = new ArrayList<>();

        if (amount.compareTo(new BigDecimal("5000")) >= 0) {
            score += 35;
            reasons.add("Large amount: payments of $5,000 or more add 35 risk points.");
        } else if (amount.compareTo(new BigDecimal("1000")) >= 0) {
            score += 15;
            reasons.add("Higher amount: payments of $1,000 or more add 15 risk points.");
        }

        if (frequency >= 10) {
            score += 35;
            reasons.add("Many recent payments: 10 or more in the last hour add 35 risk points.");
        } else if (frequency >= 5) {
            score += 20;
            reasons.add("Several recent payments: 5 or more in the last hour add 20 risk points.");
        }

        int hour = transactionTime.getHour();
        if (hour >= 23 || hour < 5) {
            score += 20;
            reasons.add("Unusual hour: payments between 11 PM and 5 AM add 20 risk points.");
        }

        if ("Other".equalsIgnoreCase(device)) {
            score += 10;
            reasons.add("Unrecognized device type: this adds 10 risk points.");
        }

        String normalizedLocation = location.toLowerCase(Locale.ROOT);
        if (normalizedLocation.contains("unknown")) {
            score += 20;
            reasons.add("Unknown location: this adds 20 risk points.");
        }
        if (normalizedLocation.contains("international")) {
            score += 15;
            reasons.add("International location: this adds 15 risk points.");
        }

        score = Math.min(score, 100);
        if (reasons.isEmpty()) {
            reasons.add("No elevated-risk rules were triggered.");
        }

        String status = score >= 60 ? "Blocked" : score >= 30 ? "Flagged" : "Approved";
        return new RiskResult(score, status, List.copyOf(reasons));
    }

    public record RiskResult(int score, String status, List<String> reasons) {}
}
