package com.SwiftPay.SwiftPayApp.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.SwiftPay.SwiftPayApp.DTO.PaymentRequest;
import com.SwiftPay.SwiftPayApp.DTO.PaymentResponse;
import com.SwiftPay.SwiftPayApp.config.PaymentProducer;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentProducer producer;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void shouldCreatePaymentWhenRequestIsValid() {
        PaymentRequest request = new PaymentRequest(1L, 2L, new BigDecimal("125.50"), "USD");

        PaymentResponse response = paymentService.createPayment(request);

        assertNotNull(response);
        assertNotNull(response.getTransactionId());
        assertEquals("PENDING", response.getStatus());
        assertEquals("Payment Created Successfully", response.getMessage());
        verify(producer).sendPaymentMessage(request);
    }

    @Test
    void shouldFailWhenRequestIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> paymentService.createPayment(null));
        assertEquals("Payment request cannot be null", exception.getMessage());
        verify(producer, never()).sendPaymentMessage(any());
    }

    @Test
    void shouldFailWhenSenderIdIsMissing() {
        PaymentRequest request = new PaymentRequest(null, 2L, new BigDecimal("50"), "USD");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> paymentService.createPayment(request));
        assertEquals("SenderId is required", exception.getMessage());
        verify(producer, never()).sendPaymentMessage(any());
    }

    @Test
    void shouldFailWhenReceiverIdIsMissing() {
        PaymentRequest request = new PaymentRequest(1L, null, new BigDecimal("50"), "USD");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> paymentService.createPayment(request));
        assertEquals("ReceiverId is required", exception.getMessage());
        verify(producer, never()).sendPaymentMessage(any());
    }

    @Test
    void shouldFailWhenSenderAndReceiverAreSame() {
        PaymentRequest request = new PaymentRequest(1L, 1L, new BigDecimal("50"), "USD");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> paymentService.createPayment(request));
        assertEquals("SenderId and ReceiverId must be different", exception.getMessage());
        verify(producer, never()).sendPaymentMessage(any());
    }

    @Test
    void shouldFailWhenAmountIsInvalid() {
        PaymentRequest request = new PaymentRequest(1L, 2L, BigDecimal.ZERO, "USD");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> paymentService.createPayment(request));
        assertEquals("Amount must be greater than zero", exception.getMessage());
        verify(producer, never()).sendPaymentMessage(any());
    }

    @Test
    void shouldFailWhenCurrencyIsMissing() {
        PaymentRequest request = new PaymentRequest(1L, 2L, new BigDecimal("50"), "");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> paymentService.createPayment(request));
        assertEquals("Currency is required", exception.getMessage());
        verify(producer, never()).sendPaymentMessage(any());
    }

    @Test
    void shouldWrapProducerFailure() {
        PaymentRequest request = new PaymentRequest(1L, 2L, new BigDecimal("80"), "EUR");
        doThrow(new RuntimeException("Kafka temporarily unavailable")).when(producer).sendPaymentMessage(any());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> paymentService.createPayment(request));
        assertEquals("Unable to publish payment event", exception.getMessage());
        verify(producer).sendPaymentMessage(request);
    }
}
