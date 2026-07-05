package com.SwiftPay.SwiftPayApp.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.SwiftPay.SwiftPayApp.DTO.PaymentRequest;
import com.SwiftPay.SwiftPayApp.DTO.PaymentResponse;
import com.SwiftPay.SwiftPayApp.Entity.Payment;
import com.SwiftPay.SwiftPayApp.config.PaymentProducer;
import com.SwiftPay.SwiftPayApp.enums.PaymentStatus;
import com.SwiftPay.SwiftPayApp.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentProducer producer;

    @Override
    public PaymentResponse createPayment(PaymentRequest request) {
        validateRequest(request);

        log.info("Processing payment request senderId={} receiverId={} amount={} currency={}", request.getSenderId(), request.getReceiverId(), request.getAmount(), request.getCurrency());

        try {
            producer.sendPaymentMessage(request);
        } catch (Exception ex) {
            log.error("Failed to publish payment event for senderId={} receiverId={}", request.getSenderId(), request.getReceiverId(), ex);
            throw new IllegalStateException("Unable to publish payment event", ex);
        }

        Payment payment = Payment.builder()
                .transactionId(UUID.randomUUID())
                .senderId(request.getSenderId())
                .receiverId(request.getReceiverId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        log.info("Created payment model transactionId={}", payment.getTransactionId());

        return PaymentResponse.builder()
                .transactionId(payment.getTransactionId())
                .status(payment.getStatus().name())
                .message("Payment Created Successfully")
                .build();
    }

    private void validateRequest(PaymentRequest request) {
        if (request == null) {
            log.error("PaymentRequest is null");
            throw new IllegalArgumentException("Payment request cannot be null");
        }
        if (request.getSenderId() == null) {
            log.error("PaymentRequest senderId is missing: {}", request);
            throw new IllegalArgumentException("SenderId is required");
        }
        if (request.getReceiverId() == null) {
            log.error("PaymentRequest receiverId is missing: {}", request);
            throw new IllegalArgumentException("ReceiverId is required");
        }
        if (request.getSenderId().equals(request.getReceiverId())) {
            log.error("PaymentRequest senderId and receiverId are equal: {}", request);
            throw new IllegalArgumentException("SenderId and ReceiverId must be different");
        }
        if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            log.error("PaymentRequest amount is invalid: {}", request);
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (request.getCurrency() == null || request.getCurrency().isBlank()) {
            log.error("PaymentRequest currency is missing: {}", request);
            throw new IllegalArgumentException("Currency is required");
        }
    }
}