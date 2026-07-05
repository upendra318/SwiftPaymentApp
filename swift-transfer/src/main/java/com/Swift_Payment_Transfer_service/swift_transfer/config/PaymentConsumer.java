package com.Swift_Payment_Transfer_service.swift_transfer.config;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.Swift_Payment_Transfer_service.swift_transfer.DTO.PaymentRequest;
import com.Swift_Payment_Transfer_service.swift_transfer.service.SwiftTransferService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentConsumer {

    private final SwiftTransferService swiftTransferService;

    @KafkaListener(
            topics = "payment-initiated",
            containerFactory = "kafkaListenerContainerFactory")
    public void consume(ConsumerRecord<String, PaymentRequest> record, Acknowledgment acknowledgment) {
        PaymentRequest event = record.value();
        log.info("Received payment event topic={} partition={} offset={} key={}", record.topic(), record.partition(), record.offset(), record.key());

        if (event == null) {
            log.warn("Skipping null payment message at offset={} partition={}", record.offset(), record.partition());
            acknowledgment.acknowledge();
            return;
        }

        if (event.getMessageId() == null || event.getMessageId().isBlank()) {
            event.setMessageId(record.key());
        }

        try {
            swiftTransferService.transferPayment(event);
            acknowledgment.acknowledge();
            log.info("Acknowledged payment event offset={}", record.offset());
        } catch (Exception ex) {
            log.error("Failed to process payment event offset={} key={}", record.offset(), record.key(), ex);
            throw ex;
        }
    }
}
