package com.example.ct_order_demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CtOrderDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(CtOrderDemoApplication.class, args);
	}

}
