package com.SwiftPay.SwiftPayApp.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.SwiftPay.SwiftPayApp.DTO.PaymentRequest;

@ExtendWith(MockitoExtension.class)
class PaymentProducerTest {

    @Mock
    private KafkaTemplate<String, PaymentRequest> kafkaTemplate;

    @InjectMocks
    private PaymentProducer paymentProducer;

    private PaymentRequest paymentRequest;

    @BeforeEach
    void setup() {

        paymentRequest = new PaymentRequest();

        paymentRequest.setSenderId(1001L);
        paymentRequest.setReceiverId(1002L);
        paymentRequest.setAmount(new BigDecimal("500"));
        paymentRequest.setCurrency("INR");
    }

    @Test
    void shouldNotSendMessageWhenRequestIsNull() {

        paymentProducer.sendPaymentMessage(null);

        verify(kafkaTemplate, never()).send(any(ProducerRecord.class));
    }

    @Test
    void shouldNotSendMessageWhenSenderIdIsNull() {

        paymentRequest.setSenderId(null);

        paymentProducer.sendPaymentMessage(paymentRequest);

        verify(kafkaTemplate, never()).send(any(ProducerRecord.class));
    }

    @Test
    void shouldNotSendMessageWhenReceiverIdIsNull() {

        paymentRequest.setReceiverId(null);

        paymentProducer.sendPaymentMessage(paymentRequest);

        verify(kafkaTemplate, never()).send(any(ProducerRecord.class));
    }

    @Test
    void shouldNotSendMessageWhenAmountIsNull() {

        paymentRequest.setAmount(null);

        paymentProducer.sendPaymentMessage(paymentRequest);

        verify(kafkaTemplate, never()).send(any(ProducerRecord.class));
    }

    @Test
    void shouldNotSendMessageWhenAmountIsZero() {

        paymentRequest.setAmount(BigDecimal.ZERO);

        paymentProducer.sendPaymentMessage(paymentRequest);

        verify(kafkaTemplate, never()).send(any(ProducerRecord.class));
    }

    @Test
    void shouldNotSendMessageWhenAmountIsNegative() {

        paymentRequest.setAmount(new BigDecimal("-100"));

        paymentProducer.sendPaymentMessage(paymentRequest);

        verify(kafkaTemplate, never()).send(any(ProducerRecord.class));
    }

    @Test
    void shouldSendMessageSuccessfully() {

        CompletableFuture<SendResult<String, PaymentRequest>> future =
                CompletableFuture.completedFuture(null);

        when(kafkaTemplate.send(any(ProducerRecord.class)))
                .thenReturn(future);

        paymentProducer.sendPaymentMessage(paymentRequest);

        verify(kafkaTemplate, times(1))
                .send(any(ProducerRecord.class));
    }

    @Test
    void shouldHandleKafkaException() {

        when(kafkaTemplate.send(any(ProducerRecord.class)))
                .thenThrow(new RuntimeException("Kafka Down"));

        paymentProducer.sendPaymentMessage(paymentRequest);

        verify(kafkaTemplate).send(any(ProducerRecord.class));
    }

    @Test
    void shouldHandleAsyncFailure() {

        CompletableFuture<SendResult<String, PaymentRequest>> future =
                new CompletableFuture<>();

        future.completeExceptionally(
                new RuntimeException("Kafka Failure"));

        when(kafkaTemplate.send(any(ProducerRecord.class)))
                .thenReturn(future);

        paymentProducer.sendPaymentMessage(paymentRequest);

        verify(kafkaTemplate).send(any(ProducerRecord.class));
    }

}