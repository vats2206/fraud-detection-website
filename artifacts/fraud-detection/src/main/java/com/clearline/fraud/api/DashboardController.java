package com.clearline.fraud.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clearline.fraud.api.ApiDtos.DashboardSummary;
import com.clearline.fraud.model.TransactionRecord;
import com.clearline.fraud.repository.TransactionRepository;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/risk-api/dashboard")
public class DashboardController {
    private final TransactionRepository transactions;

    public DashboardController(TransactionRepository transactions) {
        this.transactions = transactions;
    }

    @GetMapping
    public DashboardSummary summary(HttpSession session) {
        Long userId = AuthController.requireUserId(session);
        List<TransactionRecord> rows = transactions.findByUserIdOrderByCreatedAtDesc(userId);
        long approved = rows.stream().filter(row -> "Approved".equals(row.getStatus())).count();
        long flagged = rows.stream().filter(row -> "Flagged".equals(row.getStatus())).count();
        long blocked = rows.stream().filter(row -> "Blocked".equals(row.getStatus())).count();
        double average = rows.stream()
                .mapToInt(TransactionRecord::getRiskScore)
                .average()
                .orElse(0);
        return new DashboardSummary(rows.size(), approved, flagged, blocked, average);
    }
}
