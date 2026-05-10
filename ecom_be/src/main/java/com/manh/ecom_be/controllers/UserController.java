package com.manh.ecom_be.controllers;


import com.manh.ecom_be.components.JwtTokenUtils;
import com.manh.ecom_be.dtos.UserDTO;
import com.manh.ecom_be.dtos.UserLoginDTO;
import com.manh.ecom_be.models.Token;
import com.manh.ecom_be.models.User;
import com.manh.ecom_be.responses.LoginResponse;
import com.manh.ecom_be.responses.ResponseObject;
import com.manh.ecom_be.responses.UserResponse;
import com.manh.ecom_be.services.auth.AuthService;
import com.manh.ecom_be.services.token.InterfaceTokenService;
import com.manh.ecom_be.services.user.InterfaceUserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-key")
public class UserController {
    private final InterfaceUserService userService;
    private final InterfaceTokenService tokenService;
    private final JwtTokenUtils jwtTokenUtils;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ResponseObject> createUser(
            @Valid @RequestBody UserDTO userDTO,
            BindingResult result
    ) throws Exception {
        if (result.hasErrors()) {
            String errors = result.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            return ResponseEntiry.badRequest().body(
                    ResponseObject.builder().message(errors).status(HttpStatus.BAD_REQUEST).build()
            );
        }
        User user = userService.createUser(userDTO);
        return ResponseEntity.ok(ResponseObject.builder()
                .status(HttpStatus.CREATED)
                .message("Register successfully")
                .data(UserResponse.fromUser(user))
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseObject> login(
            @Valid @RequestBody UserLoginDTO loginDTO,
            HttpServletRequest request
    ) throws Exception {
        String userAgent = request.getHeader("User-Agent");
        boolean isMobile = userAgent != null &&
                (userAgent.contains("Mobile") || userAgent.contains("Android"));

        Token savedToken = tokenService.addToken(user, token, isMobile);

        return ResponseEntity.ok(ResponseObject.builder()
                .message("Login successfully")
                .status(HttpStatus.OK)
                .data(LoginResponse.builder()
                        .token(savedToken.getToken)
                        .tokenType("Bearer")
                        .refreshToken(savedToken.getRefreshToken())
                        .username(user.getUsername())
                        .roles(List.of(user.getRole().getName()))
                        .id(user.getId())
                        .build())
                .build());
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<ResponseObject> refreshToken(
            @RequestBody RefreshTokenDTO dto
    ) throws Exception {
        Token existingToken = tokenRepository.findByRefreshToken(dto.getRefreshToken())
                .orElseThrow(() -> new DataNotFoundException("Refresh token not found"));

        User user = existingToken.getUser();

        Token updatedToken = tokenService.refreshToken(dto.getRefreshToken(), user);
        return ResponseEntity.ok(ResponseObject.builder()
                .message("Refresh token successfully"))
                .data(LoginResponse.builder()
                        .token(updatedToken.getToken())
                        .refreshToken(updatedToken.getRefreshToken()
                                .build())
                        .build());
    }

    @GetMapping("/details")
    public ResponseEntity<ResponseObject> getUserDetails(
            @RequestHeader("Authorization") String authHeader
    ) throws Exception {
        String token = authHeader.substring(7);
        User user = userService.getUserDetailsFromToken(token);
        return ResponseEntity.ok(ResponseObject.builder()
                .data(UserResponse.fromUser(user))
                .message("OK")
                .build());
    }

    @GetMapping("/auth/social-login")
    public ResponseEntity<String> socialAuth(
            @RequestParam("login_type") String loginType
    ) {
        String url = authService.generateAuthUrl(loginType.trim().toLowerCase());
        return ResponseEntity.ok(url);
    }

    @GetMapping("/auth/social/callback")
    public ResponseEntity<ResponseObject> socialCallback(
            @RequestParam("code") String code,
            @RequestParam("login_type") String loginType,
            HttpServletRequest request
    ) throws Exception {
        Map<String, Object> userInfo = authService.authenticateAndFetchProfile(code, loginType);
        if (userInfo == null) {
            return ResponseEntity.badRequest().body(
            ResponseObject.builder().message("Failed to authenticate").build()

            );
        }
        String accountId, name, picture = "", email = "";

        if (loginType.trim().equalsIgnoreCase("google")) {
            accountId = (String) Objects.requireNonNullElse(userInfo.get("sub"), "");
            name = (String) Objects.requireNonNullElse(userInfo.get("name"), "");
            picture = (String) Objects.requireNonNullElse(userInfo.get("picture"), "");
            email = (String) Objects.requireNonNullElse(userInfo.get("email"), "");
        } else {
            accountId = (String) Objects.requireNonNullElse(userInfo.get("id"), "");
            name = (String) Objects.requireNonNullElse(userInfo.get("name"), "");
            email = (String) Objects.requireNonNullElse(userInfo.get("email"), "");
        }

        UserLoginDTO socialDTO = UserLoginDTO.builder()
                .email(email)
                .fullname(name)
                .password("")
                .profileImage(picture)
                .build();

        if (loginType.trim().equalsIgnoreCase("google")) {
            socialDTO.setGoogleAccountId(accountId);
        } else {
            socialDTO.setFacebookAccountId(accountId);
        }

        String token = userService.loginSocial(socialDTO);
        User user = userService.getUserDetailsFromToken(token);
        String userAgent = request.getHeader("User-Agent");
        boolean isMobile = userAgent != null && userAgent.toLowerCase().contains("mobile");
        Token savedToken = tokenService.addToken(user, token, isMobile);


        LoginResponse loginResponse = LoginResponse.builder()
                .token(savedToken.getToken())
                .tokenType("Bearer")
                .refreshToken(savedToken.getRefreshToken())
                .username(user.getUsername())
                .roles(user.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority).toList())
                .id(user.getId())
                .build();

        return ResponseEntity.ok(ResponseObject.builder()
                .message("Login successfully")
                .data(loginResponse)
                .status(HttpStatus.OK)
                .build());
    }
}
