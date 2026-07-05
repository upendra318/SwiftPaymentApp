package com.Swift_Payment_Transfer_service.swift_transfer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.Swift_Payment_Transfer_service.swift_transfer.DTO.PaymentRequest;
import com.Swift_Payment_Transfer_service.swift_transfer.Entity.Payment;
import com.Swift_Payment_Transfer_service.swift_transfer.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
class SwiftTransferServiceTest {

    @Mock
    private PaymentRepository repository;

    @InjectMocks
    private SwiftTransferService transferService;

    @Test
    void shouldPersistPaymentWhenRequestIsValidAndNotDuplicate() {
        PaymentRequest request = PaymentRequest.builder()
                .messageId("msg-123")
                .senderId(1L)
                .receiverId(2L)
                .amount(new BigDecimal("150.00"))
                .currency("USD")
                .build();

        when(repository.existsByMessageId("msg-123")).thenReturn(false);
        when(repository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        transferService.transferPayment(request);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(repository).save(paymentCaptor.capture());

        Payment saved = paymentCaptor.getValue();
        assertEquals(request.getMessageId(), saved.getMessageId());
        assertEquals(request.getSenderId(), saved.getSenderId());
        assertEquals(request.getReceiverId(), saved.getReceiverId());
        assertEquals(request.getAmount(), saved.getAmount());
        assertEquals(request.getCurrency(), saved.getCurrency());
        assertEquals("PENDING", saved.getStatus().name());
    }

    @Test
    void shouldSkipSavingWhenMessageIdIsDuplicate() {
        PaymentRequest request = PaymentRequest.builder()
                .messageId("duplicate-msg")
                .senderId(1L)
                .receiverId(2L)
                .amount(new BigDecimal("75.00"))
                .currency("EUR")
                .build();

        when(repository.existsByMessageId("duplicate-msg")).thenReturn(true);

        transferService.transferPayment(request);

        verify(repository, never()).save(any(Payment.class));
    }

    @Test
    void shouldThrowWhenRequestIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> transferService.transferPayment(null));
        assertEquals("PaymentRequest cannot be null", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowWhenSenderIdOrReceiverIdIsMissing() {
        PaymentRequest request = PaymentRequest.builder()
                .messageId("msg")
                .senderId(null)
                .receiverId(2L)
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> transferService.transferPayment(request));
        assertEquals("SenderId and receiverId are required", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowWhenSenderIdEqualsReceiverId() {
        PaymentRequest request = PaymentRequest.builder()
                .messageId("msg")
                .senderId(1L)
                .receiverId(1L)
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> transferService.transferPayment(request));
        assertEquals("SenderId and receiverId must be different", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowWhenAmountIsNotPositive() {
        PaymentRequest request = PaymentRequest.builder()
                .messageId("msg")
                .senderId(1L)
                .receiverId(2L)
                .amount(BigDecimal.ZERO)
                .currency("USD")
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> transferService.transferPayment(request));
        assertEquals("Amount must be a positive value", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowWhenCurrencyIsMissing() {
        PaymentRequest request = PaymentRequest.builder()
                .messageId("msg")
                .senderId(1L)
                .receiverId(2L)
                .amount(new BigDecimal("100.00"))
                .currency("")
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> transferService.transferPayment(request));
        assertEquals("Currency is required", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldRethrowWhenRepositorySaveFails() {
        PaymentRequest request = PaymentRequest.builder()
                .messageId("msg-123")
                .senderId(1L)
                .receiverId(2L)
                .amount(new BigDecimal("150.00"))
                .currency("USD")
                .build();

        when(repository.existsByMessageId("msg-123")).thenReturn(false);
        doThrow(new RuntimeException("DB down")).when(repository).save(any(Payment.class));

        assertThrows(RuntimeException.class, () -> transferService.transferPayment(request));
        verify(repository).save(any(Payment.class));
    }
}
