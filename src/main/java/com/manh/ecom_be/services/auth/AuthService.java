package com.manh.ecom_be.services.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

@Service
@Slf4j
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

    @Value("${spring.security.oauth2.client.registration.facebook.redirect-uri}")
    private String facebookRedirectUri;

    @Value("${spring.security.oauth2.client.registration.facebook.client-id}")
    private String facebookClientId;

    @Value("${spring.security.oauth2.client.registration.facebook.client-secret}")
    private String facebookClientSecret;

    @Value("${spring.security.oauth2.client.registration.facebook.auth-uri:https://www.facebook.com/v13.0/dialog/oauth}")
    private String facebookAuthUri;

    @Value("${spring.security.oauth2.client.registration.facebook.token-uri}")
    private String facebookTokenUri;

    @Value("${spring.security.oauth2.client.registration.facebook.user-info-uri}")
    private String facebookUserInfoUri;

    public String generateAuthUrl(String loginType) {
        String url = "";
        loginType = loginType.trim().toLowerCase();

        if ("google".equals(loginType)) {
            GoogleAuthorizationCodeRequestUrl urlBuilder = new GoogleAuthorizationCodeRequestUrl(
                    googleClientId,
                    googleRedirectUri,
                    Arrays.asList("email", "profile", "openid"));
            url = urlBuilder.build();
        } else if ("facebook".equals(loginType)) {
            url = UriComponentsBuilder
                    .fromUriString(facebookAuthUri)
                    .queryParam("client_id", facebookClientId)
                    .queryParam("redirect_uri", facebookRedirectUri)
                    .queryParam("scope", "email,public_profile")
                    .queryParam("response_type", "code")
                    .build()
                    .toUriString();
        }
        return url;
    }




    public Map<String, Object> authenticateAndFetchProfile(String code, String loginType)
            throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        String accessToken;

        switch (loginType.toLowerCase()) {
            case "google":
                // Đổi authorization code → access token
                accessToken = new GoogleAuthorizationCodeTokenRequest(
                        new NetHttpTransport(), new GsonFactory(),
                        googleClientId,
                        googleClientSecret,
                        code, googleRedirectUri
                ).execute().getAccessToken();

                // Dùng access token để lấy profile
                restTemplate.getInterceptors().add((req, body, executionContext) -> {
                    req.getHeaders().set("Authorization", "Bearer " + accessToken);
                    return executionContext.execute(req, body);
                });
                return new ObjectMapper().readValue(
                        restTemplate.getForEntity(googleUserInfoUri, String.class).getBody(),
                        new TypeReference<>() {});

            case "facebook":
                // Đổi code → access token qua URL params
                String urlGetAccessToken = UriComponentsBuilder
                        .fromUriString(facebookTokenUri)
                        .queryParam("client_id", facebookClientId)
                        .queryParam("redirect_uri", facebookRedirectUri)
                        .queryParam("client_secret", facebookClientSecret)
                        .queryParam("code", code)
                        .toUriString();

                ResponseEntity<String> response = restTemplate.getForEntity(urlGetAccessToken, String.class);
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(response.getBody());
                accessToken = node.get("access_token").asText();

                // Lấy user info từ Facebook Graph API
                String userInfoUri = facebookUserInfoUri + "&access_token=" + accessToken;
                return mapper.readValue(
                        restTemplate.getForEntity(userInfoUri, String.class).getBody(),
                        new TypeReference<>() {
                        });

            default:
                log.warn("Unsupported login type: {}", loginType);
                return null;
        }
    }
}
