package com.clearline.fraud.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class ApiDtos {
    private ApiDtos() {}

    public record RegisterRequest(
            @NotBlank(message = "Enter your name.")
            @Size(min = 2, max = 100, message = "Your name must be between 2 and 100 characters.")
            String name,
            @NotBlank(message = "Enter your email address.")
            @Email(message = "Enter a valid email address.")
            @Size(max = 254, message = "Your email address is too long.")
            String email,
            @NotBlank(message = "Enter a password.")
            @Size(min = 8, max = 72, message = "Use a password between 8 and 72 characters.")
            String password) {}

    public record LoginRequest(
            @NotBlank(message = "Enter your email address.")
            @Email(message = "Enter a valid email address.")
            String email,
            @NotBlank(message = "Enter your password.")
            @Size(min = 8, max = 72, message = "Use a password between 8 and 72 characters.")
            String password) {}

    public record UserView(Long id, String name, String email) {}

    public record TransactionRequest(
            @NotNull(message = "Enter an amount greater than zero.")
            @DecimalMin(value = "0.01", message = "Enter an amount greater than zero.")
            @DecimalMax(value = "100000000.00", message = "The amount is too large.")
            BigDecimal amount,
            @NotBlank(message = "Enter the payment location.")
            @Size(max = 120, message = "The location must be 120 characters or fewer.")
            String location,
            @NotBlank(message = "Choose the device used.")
            @Pattern(regexp = "Mobile|Desktop|Tablet|Other", message = "Choose a valid device.")
            String device,
            @NotBlank(message = "Choose the payment time.")
            String time,
            @NotNull(message = "Enter the recent payment frequency.")
            @Min(value = 1, message = "Frequency must be at least 1.")
            @Max(value = 100, message = "Frequency cannot be greater than 100.")
            Integer frequency) {}

    public record TransactionView(
            Long id,
            BigDecimal amount,
            String location,
            String device,
            LocalDateTime transactionTime,
            Integer frequency,
            Integer riskScore,
            String status,
            List<String> reasons,
            LocalDateTime createdAt) {}

    public record DashboardSummary(
            long totalTransactions,
            long approvedCount,
            long flaggedCount,
            long blockedCount,
            double averageRiskScore) {}
}
