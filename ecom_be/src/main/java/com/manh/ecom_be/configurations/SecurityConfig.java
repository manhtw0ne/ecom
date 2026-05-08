package com.manh.ecom_be.configurations;


import com.manh.ecom_be.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository repo) {
        return username -> repo.findByPhoneNumber(username)
                .or(() -> repo.findByEmail(username))
                        .orElseThrow(() ->
                                new UsernameNotFoundException("User not found: " + username));
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}

