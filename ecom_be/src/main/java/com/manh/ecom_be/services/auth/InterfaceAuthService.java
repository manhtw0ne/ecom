package com.manh.ecom_be.services.auth;

import java.io.IOException;
import java.util.Map;

public interface InterfaceAuthService {
    String generateAuthUrl(String loginType);
    Map<String, Object> authenticateAndFetchProfile(String code, String loginType)
            throws IOException;
}
