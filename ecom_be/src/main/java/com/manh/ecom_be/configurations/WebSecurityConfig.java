package com.manh.ecom_be.configurations;


import com.manh.ecom_be.filters.JwtTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import static org.springframework.http.HttpMethod.*;



@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final JwtTokenFilter jwtTokenFilter;

    @Value("${api.prefix}")
    private String apiPrefix;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(
                        c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtTokenFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(req -> req.requestMatchers(
                                apiPrefix + "/users/register",
                                apiPrefix + "/users/login",
                                apiPrefix + "/users/refreshToken",
                                apiPrefix + "/users/auth/social-login",
                                apiPrefix + "/users/auth/social/callback",
                                "/api-docs", "/api-docs/**",
                                "/swagger-resources/**",
                                "/swagger-ui/**", "/swagger-ui.html"
                        ).permitAll()

                        // Public GET
                        .requestMatchers(GET,
                                apiPrefix + "/products/**",
                                apiPrefix + "/categories/**",
                                apiPrefix + "/comments**",
                                apiPrefix + "/roles**",
                                apiPrefix + "/coupons**",
                                apiPrefix + "/healthcheck/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(Customizer.withDefaults());
        return http.build();
    }
}
