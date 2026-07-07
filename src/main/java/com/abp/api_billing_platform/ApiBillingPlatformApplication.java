package com.abp.api_billing_platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ApiBillingPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiBillingPlatformApplication.class, args);
	}

}
