package com.manh.ecom_be.configurations;


import com.manh.ecom_be.filters.JwtTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import static org.springframework.http.HttpMethod.*;



@Configuration
@EnableMethodSecurity
@EnableWebSecurity(debug = true)
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final JwtTokenFilter jwtTokenFilter;

    @Value("${api.prefix}")
    private String apiPrefix;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(
                        c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtTokenFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(customizer -> customizer
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(
                                String.format("%s/users/register", apiPrefix),
                                String.format("%s/users/login", apiPrefix),
                                // Healthcheck
                                String.format("%s/healthcheck/**", apiPrefix),
                                // Actuator
                                String.format("%s/actuator/**", apiPrefix),
                                // Swagger
                                "/api-docs",
                                "/api-docs/**",
                                "/swagger-resources",
                                "/swagger-resources/**",
                                "/configuration/ui",
                                "/configuration/security",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/webjars/swagger-ui/**",
                                "/swagger-ui/index.html",
                                // Social login
                                String.format("%s/users/auth/social-login", apiPrefix),
                                String.format("%s/users/auth/social/callback", apiPrefix)
                        ).permitAll()
                        .requestMatchers(
                                GET,
                                String.format("%s/roles**", apiPrefix)).permitAll()
                                .requestMatchers(GET,
                                        String.format("%s/policies/**", apiPrefix)).permitAll()
                                .requestMatchers(GET,
                                        String.format("%s/categories/**", apiPrefix)).permitAll()
                                .requestMatchers(GET,
                                        String.format("%s/products/**", apiPrefix)).permitAll()
                                .requestMatchers(GET,
                                        String.format("%s/products/images/*", apiPrefix)).permitAll()
                                .requestMatchers(GET,
                                        String.format("%s/orders/**", apiPrefix)).permitAll()
                                .requestMatchers(GET,
                                        String.format("%s/users/profile-images/**", apiPrefix)).permitAll()
                                .requestMatchers(GET,
                                        String.format("%s/order_details/**", apiPrefix)).permitAll()
                                .anyRequest().authenticated()
                        )
                .oauth2Login(Customizer.withDefaults())
                .oauth2ResourceServer(c -> c.opaqueToken(Customizer.withDefaults()));
        return http.build();
    }
}
