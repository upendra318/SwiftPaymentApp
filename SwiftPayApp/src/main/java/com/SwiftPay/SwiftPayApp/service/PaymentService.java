package com.SwiftPay.SwiftPayApp.service;

import com.SwiftPay.SwiftPayApp.DTO.PaymentRequest;
import com.SwiftPay.SwiftPayApp.DTO.PaymentResponse;

public interface PaymentService {

    PaymentResponse createPayment(PaymentRequest request);

}
