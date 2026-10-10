package com.clearline.fraud.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "transactions")
public class TransactionRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 120)
    private String location;

    @Column(nullable = false, length = 20)
    private String device;

    @Column(name = "transaction_time", nullable = false)
    private LocalDateTime transactionTime;

    @Column(nullable = false)
    private Integer frequency;

    @Column(name = "risk_score", nullable = false)
    private Integer riskScore;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "reasons_json", nullable = false, columnDefinition = "TEXT")
    private String reasonsJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected TransactionRecord() {}

    public TransactionRecord(
            Long userId,
            BigDecimal amount,
            String location,
            String device,
            LocalDateTime transactionTime,
            Integer frequency,
            Integer riskScore,
            String status,
            String reasonsJson) {
        this.userId = userId;
        this.amount = amount;
        this.location = location;
        this.device = device;
        this.transactionTime = transactionTime;
        this.frequency = frequency;
        this.riskScore = riskScore;
        this.status = status;
        this.reasonsJson = reasonsJson;
    }

    @PrePersist
    void setCreatedAt() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public BigDecimal getAmount() { return amount; }
    public String getLocation() { return location; }
    public String getDevice() { return device; }
    public LocalDateTime getTransactionTime() { return transactionTime; }
    public Integer getFrequency() { return frequency; }
    public Integer getRiskScore() { return riskScore; }
    public String getStatus() { return status; }
    public String getReasonsJson() { return reasonsJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
