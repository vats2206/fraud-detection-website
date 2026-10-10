package com.clearline.fraud.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.clearline.fraud.model.TransactionRecord;

public interface TransactionRepository extends JpaRepository<TransactionRecord, Long> {
    List<TransactionRecord> findByUserIdOrderByCreatedAtDesc(Long userId);
}
