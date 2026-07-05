package com.SwiftPay.SwiftPayApp.config;

import java.util.concurrent.CompletableFuture;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.hibernate.jpa.event.internal.ListenerCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import com.SwiftPay.SwiftPayApp.DTO.PaymentRequest;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentProducer {

    private static final Logger log = LoggerFactory.getLogger(PaymentProducer.class);
    private static final String TOPIC = "payment-initiated";

    private final KafkaTemplate<String, PaymentRequest> kafkaTemplate;

    public void sendPaymentMessage(PaymentRequest message) {
        if (message == null) {
            log.error("PaymentRequest is null, cannot send message");
            return;
        }

        if (message.getSenderId() == null) {
            log.error("PaymentRequest senderId is missing: {}", message);
            return;
        }

        if (message.getReceiverId() == null) {
            log.error("PaymentRequest receiverId is missing: {}", message);
            return;
        }

        if (message.getAmount() == null || message.getAmount().signum() <= 0) {
            log.error("PaymentRequest amount is invalid: {}", message);
            return;
        }

        String key = message.getSenderId().toString();


        try {
            CompletableFuture<SendResult<String, PaymentRequest>> future = kafkaTemplate.send(new ProducerRecord<>(TOPIC, key, message));
            future.whenComplete((result, ex) -> {

                if (ex == null) {

                    RecordMetadata metadata = result.getRecordMetadata();

                    log.info(
                            "Payment message sent successfully topic={} partition={} offset={} key={}",
                            metadata.topic(),
                            metadata.partition(),
                            metadata.offset(),
                            key);

                } else {

                    log.error(
                            "Payment message send failed for key={} topic={}",
                            key,
                            TOPIC,
                            ex);

                }

            });
        } catch (Exception ex) {
            log.error("Exception while sending payment message key={} topic={}", key, TOPIC, ex);
        }
    }
}
