package com.Swift_Payment_Transfer_service.swift_transfer.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Swift_Payment_Transfer_service.swift_transfer.DTO.PaymentRequest;
import com.Swift_Payment_Transfer_service.swift_transfer.Entity.Payment;
import com.Swift_Payment_Transfer_service.swift_transfer.enums.PaymentStatus;
import com.Swift_Payment_Transfer_service.swift_transfer.repository.PaymentRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class SwiftTransferService {
    private final PaymentRepository repository;

    @Transactional
    public void transferPayment(PaymentRequest request) {
        validateRequest(request);

        if (request.getMessageId() != null && repository.existsByMessageId(request.getMessageId())) {
            log.warn("Duplicate payment request skipped for messageId={}", request.getMessageId());
            return;
        }

        Payment payment = Payment.builder()
                .transactionId(UUID.randomUUID())
                .messageId(request.getMessageId())
                .senderId(request.getSenderId())
                .receiverId(request.getReceiverId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        try {
            repository.save(payment);
            log.info("Payment persisted with transactionId={} messageId={}", payment.getTransactionId(), payment.getMessageId());
        } catch (Exception ex) {
            log.error("Unable to persist payment for request messageId={} senderId={} receiverId={}", request.getMessageId(), request.getSenderId(), request.getReceiverId(), ex);
            throw ex;
        }
    }

    private void validateRequest(PaymentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("PaymentRequest cannot be null");
        }
        if (request.getSenderId() == null || request.getReceiverId() == null) {
            throw new IllegalArgumentException("SenderId and receiverId are required");
        }
        if (request.getSenderId().equals(request.getReceiverId())) {
            throw new IllegalArgumentException("SenderId and receiverId must be different");
        }
        if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("Amount must be a positive value");
        }
        if (request.getCurrency() == null || request.getCurrency().isBlank()) {
            throw new IllegalArgumentException("Currency is required");
        }
    }
}
