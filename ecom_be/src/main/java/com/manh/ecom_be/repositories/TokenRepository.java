package com.manh.ecom_be.repositories;

import com.manh.ecom_be.models.Token;
import com.manh.ecom_be.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    List<Token> findByUser(User user);
    Optional<Token> findByToken(String token);
    Optional<Token> findByRefreshToken(String refreshToken);
}
