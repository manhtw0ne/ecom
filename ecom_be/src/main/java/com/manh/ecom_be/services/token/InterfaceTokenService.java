package com.manh.ecom_be.services.token;

import com.manh.ecom_be.models.Token;
import com.manh.ecom_be.models.User;

public interface InterfaceTokenService {
    Token addToken(User user, String token, boolean isMobileDevice) throws Exception;
    Token refreshToken(String refreshToken, User user) throws Exception;
}
