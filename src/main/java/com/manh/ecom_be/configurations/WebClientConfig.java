package com.manh.ecom_be.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient userInfoClient() {
        return WebClient.builder()
                .baseUrl("https://www.googleapis.com")
                .build();
    }
}
