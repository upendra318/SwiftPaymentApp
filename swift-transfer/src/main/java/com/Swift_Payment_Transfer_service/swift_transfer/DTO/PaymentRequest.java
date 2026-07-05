package com.Swift_Payment_Transfer_service.swift_transfer.DTO;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentRequest {
    private String messageId;
    private Long senderId;
    private Long receiverId;
    private BigDecimal amount;
    private String currency;
}

