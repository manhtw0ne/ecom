package com.manh.ecom_be.components;


import com.manh.ecom_be.models.Token;
import com.manh.ecom_be.models.User;
import com.manh.ecom_be.repositories.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtTokenUtils {
    @Value("${jwt.expiration")
    private int expiration;

    @Value("${jwt.expiration-refresh-token")
    private int expirationRefreshToken;

    @Value("${jwt.secretKey}")
    private String secretKey;

    private final TokenRepository tokenRepository;

    public String generateToken(User user) throws Exception {
        Map<String, Object> claims = new HashMap<>();
        String subject = getSubject(user);
        claims.put("subject", subject);
        claims.put("userId", user.getId());

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .expiration(new Date(System.currentTimeMillis() + expiration * 1000L))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String getSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean validateToken(String token, User user) throws Exception {
        String subject = getSubject(token);
        Optional<Token> optToken = tokenRepository.findByToken(token);
        if (optToken.isEmpty() || optToken.get().isRevoked() || optToken.get().isExpired()) {
            return false;
        }
        return subject.equals(user.getUsername()) && !isTokenExpired(token);
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(
                Jwts.parser().verifyWith(getSignInKey()).build()
                        .parserSignedClaims(token).getPayload()
        );
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before
                (new Date());
    }

    private SecretKey getSignInKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    public int getExpiration() {return expiration;}
    public int getExpirationRefreshToken() {return expirationRefreshToken;}

    private static String getSubject(User user) {
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isBlank())
            return user.getPhoneNumber();
        return user.getEmail();
    }
}
