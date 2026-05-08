package com.manh.ecom_be.components;


import com.manh.ecom_be.models.User;
import org.springframework.security.core.Authentication;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.net.Authenticator;

@Component
public class SecurityUtils {
    public User getLoggedInUser() {
        Authenticator auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return (User) auth.getPrincipal();
    }
}
