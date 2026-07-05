package com.Swift_Payment_Transfer_service.swift_transfer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class SwiftTransferApplication {

	public static void main(String[] args) {
		SpringApplication.run(SwiftTransferApplication.class, args);
	}

}
