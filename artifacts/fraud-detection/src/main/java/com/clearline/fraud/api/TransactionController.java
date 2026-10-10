package com.clearline.fraud.api;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.clearline.fraud.api.ApiDtos.TransactionRequest;
import com.clearline.fraud.api.ApiDtos.TransactionView;
import com.clearline.fraud.model.TransactionRecord;
import com.clearline.fraud.repository.TransactionRepository;
import com.clearline.fraud.service.RiskScoringService;
import com.clearline.fraud.service.RiskScoringService.RiskResult;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/risk-api/transactions")
public class TransactionController {
    private final TransactionRepository transactions;
    private final RiskScoringService riskScoring;
    private final ObjectMapper objectMapper;

    public TransactionController(
            TransactionRepository transactions,
            RiskScoringService riskScoring,
            ObjectMapper objectMapper) {
        this.transactions = transactions;
        this.riskScoring = riskScoring;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public List<TransactionView> list(HttpSession session) {
        Long userId = AuthController.requireUserId(session);
        return transactions.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toView)
                .toList();
    }

    @PostMapping
    public TransactionView create(
            @Valid @RequestBody TransactionRequest request,
            HttpSession session) {
        Long userId = AuthController.requireUserId(session);
        LocalDateTime transactionTime;
        try {
            transactionTime = LocalDateTime.parse(request.time());
        } catch (DateTimeParseException error) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Choose a valid payment time.");
        }

        RiskResult result = riskScoring.score(
                request.amount(),
                request.location().trim(),
                request.device(),
                transactionTime,
                request.frequency());
        TransactionRecord saved = transactions.save(new TransactionRecord(
                userId,
                request.amount(),
                request.location().trim(),
                request.device(),
                transactionTime,
                request.frequency(),
                result.score(),
                result.status(),
                writeReasons(result.reasons())));
        return toView(saved);
    }

    private String writeReasons(List<String> reasons) {
        try {
            return objectMapper.writeValueAsString(reasons);
        } catch (JsonProcessingException error) {
            throw new IllegalStateException("Could not save the risk rule results.", error);
        }
    }

    private TransactionView toView(TransactionRecord transaction) {
        return new TransactionView(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getLocation(),
                transaction.getDevice(),
                transaction.getTransactionTime(),
                transaction.getFrequency(),
                transaction.getRiskScore(),
                transaction.getStatus(),
                readReasons(transaction.getReasonsJson()),
                transaction.getCreatedAt());
    }

    private List<String> readReasons(String reasonsJson) {
        try {
            return objectMapper.readValue(reasonsJson, new TypeReference<>() {});
        } catch (JsonProcessingException error) {
            return Arrays.asList("Saved risk rule details could not be read.");
        }
    }
}
