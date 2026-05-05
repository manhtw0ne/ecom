package com.manh.ecom_be.services.token;


import com.manh.ecom_be.components.JwtTokenUtils;
import com.manh.ecom_be.models.Token;
import com.manh.ecom_be.models.User;
import com.manh.ecom_be.repositories.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService implements InterfaceTokenService {
    private static final int MAX_TOKENS = 3;
    private final TokenRepository tokenRepository;
    private final JwtTokenUtils jwtTokenUtils;

    @Override
    @Transactional
    public Token addToken(User user, String token, boolean isMobileDevice) throws Exception {
        List<Token> userTokens = tokenRepository.findByUser(user);
        if (userTokens.size() >= MAX_TOKENS) {
            Token tokenToDelete = userTokens.stream()
                    .filter(t -> !t.isMobile())
                    .findFirst()
                    .orElse(userTokens.get(0));
            tokenRepository.delete(tokenToDelete);
        }

        Token newToken = Token.builder()
                .user(user)
                .token(token)
                .revoked(false)
                .expired(false)
                .tokenType("Bearer")
                .expirationDate(LocalDateTime.now().plusSeconds
                        (jwtTokenUtils.getExpiration()))
                .isMobile(isMobileDevice)
                .refreshToken(UUID.randomUUID().toString())
                .refreshExpirationDate(LocalDateTime.now()
                        .plusSeconds(jwtTokenUtils.getExpirationRefreshToken()))
                .build();
        return tokenRepository.save(newToken);
    }

    @Override
    @Transactional
    public Token refreshToken(String refreshToken, User user)
        throws Exception {
        Token existingToken = tokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new DataNotFoundException("Refresh token not found"));

        if (existingToken.getRefreshExpirationDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(existingToken);
            throw new DataNotFoundException("Refresh token expired");
        }

        String newAccesToken = jwtTokenUtils.generateToken(user);
        existingToken.setToken(newAccesToken);
        existingToken.setExpirationDate(
                LocalDateTime.now().plusSeconds(jwtTokenUtils.getExpiration())
        );

        return tokenRepository.save(existingToken);
    }
}
