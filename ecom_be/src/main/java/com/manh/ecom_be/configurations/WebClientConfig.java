package com.manh.ecom_be.configurations;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClientConfig userInfoClient() {
        return WebClient.builder()
                .baseUrl("https://www.googleapis.com")
                .build();
    }
}
