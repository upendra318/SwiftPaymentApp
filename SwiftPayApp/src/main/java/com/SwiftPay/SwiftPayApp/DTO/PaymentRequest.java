package com.SwiftPay.SwiftPayApp.DTO;

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

public class PaymentRequest {
    private Long senderId;

    private Long receiverId;

    private BigDecimal amount;

    private String currency;
}
