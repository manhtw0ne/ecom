package com.manh.ecom_be.services.auth;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService implements InterfaceAuthService {
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String googleRedirectUri;

    @Value("${spring.security.oauth2.client.registration.google.user-info-uri}")
    private String googleUserInfoUri;

    @Value("${spring.security.oauth2.client.registration.facebook.client-id}")
    private String facebookClientId;

    @Value("${spring.security.oauth2.client.registration.facebook.client-secret}")
    private String facebookClientSecret;

    @Value("${spring.security.oauth2.client.registration.facebook.redirect-uri}")
    private String facebookRedirectUri;

    @Value("${spring.security.oauth2.client.registration.facebook.auth-uri}")
    private String facebookAuthUri;

    @Value("${spring.security.oauth2.client.registration.facebook.token-uri}")
    private String facebookTokenUri;

    @Value("${spring.security.oauth2.client.registration.facebook.user-info-uri}")
    private String facebookUserInfoUri;

    @Override
    public String generateAuthUrl(String loginType) {
        loginType = loginType.trim().toLowerCase();

        if ("google".equals(loginType)) {
            return new GoogleAuthorizationCodeRequestUrl(
                    googleClientId,
                    googleRedirectUri,
                    Arrays.asList("email", "profile", "openid")
            ).build();
        } else if ("facebook".equals(loginType)) {
            return UriComponentsBuilder
                    .fromUriString(facebookAuthUri)
                    .queryParam("client_id", facebookClientId)
                    .queryParam("redirect_uri", facebookRedirectUri)
                    .queryParam("scope", "email,public_profile")
                    .queryParam("response_type", "code")
                    .build()
                    .toUriString();
        }
        return "";
    }

    @Override
    public Map<String, Object> authenticateAndFetchProfile(String code, String loginType)
        throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

        switch (loginType.trim().toLowerCase()) {
            case "google": {
                String accessToken = new GoogleAuthorizationCodeTokenRequest(
                        new NetHttpTransport(), new GsonFactory(),
                        googleClientId, googleClientSecret,
                        code, googleRedirectUri
                ).execute().getAccessToken();

                restTemplate.getInterceptors().add((req, body, exec) -> {
                    req.getHeaders().set("Authorization", "Bearer " + accessToken);
                    return exec.execute(req, body);
                });

                String body = restTemplate.getForEntity(googleUserInfoUri, String.class).getBody();
                return new ObjectMapper().readValue(body, new TypeReference<>() {});
            }

            case "facebook": {
                String tokenUrl = UriComponentsBuilder
                        .fromUriString(facebookTokenUri)
                        .queryParam("client_id", facebookClientId)
                        .queryParam("redirect_uri", facebookRedirectUri)
                        .queryParam("client_secret", facebookClientSecret)
                        .queryParam("code", code)
                        .toUriString();

                ResponseEntity<String> tokenResponse =
                        restTemplate.getForEntity(tokenUrl, String.class);
                JsonNode tokenJson = new ObjectMapper().readTree(tokenResponse.getBody());
                String accessToken = tokenJson.get("access_token").asText();

                String profileUrl = UriComponentsBuilder
                        .fromUriString(facebookUserInfoUri)
                        .queryParam("access_token", accessToken)
                        .toUriString();

                String profileBody = restTemplate.getForEntity(profileUrl, String.class).getBody();
                return new ObjectMapper().readValue(profileBody, new TypeReference<>() {
                });
            }

            default:
                throw new IllegalArgumentException("Unknown login type: " + loginType);
        }
    }
}
