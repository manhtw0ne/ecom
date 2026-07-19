package com.manh.ecom_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class EcomBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcomBeApplication.class, args);
	}

}
