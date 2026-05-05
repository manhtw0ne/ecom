package com.manh.ecom_be.controllers;


import com.manh.ecom_be.components.JwtTokenUtils;
import com.manh.ecom_be.dtos.UserDTO;
import com.manh.ecom_be.dtos.UserLoginDTO;
import com.manh.ecom_be.models.Token;
import com.manh.ecom_be.models.User;
import com.manh.ecom_be.responses.LoginResponse;
import com.manh.ecom_be.responses.ResponseObject;
import com.manh.ecom_be.responses.UserResponse;
import com.manh.ecom_be.services.token.InterfaceTokenService;
import com.manh.ecom_be.services.user.InterfaceUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {
    private final InterfaceUserService userService;
    private final InterfaceTokenService tokenService;
    private final JwtTokenUtils jwtTokenUtils;

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
}
