package com.manh.ecom_be.filters;


import com.manh.ecom_be.components.JwtTokenUtils;
import com.manh.ecom_be.models.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.security.authentication.CachingUserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {
    @Value("${api.prefix}")
    private String apiPrefix;
    private final UserDetailsService userDetailsService;
    private final JwtTokenUtils jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
        throws ServletException, IOException {
        try {
            if (isBypassToken(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            final String authHeader = request.getHeader
                    ("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer")) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing token");
                return;
            }

            final String token = authHeader.substring(7);
            final String subject = jwtTokenUtil.getSubject(token);

            if (subject != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User userDetails = (User) userDetailsService.loadUserByUsername(subject);
                if (jwtTokenUtil.validateToken(token, userDetails))
                {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities()
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(e.getMessage());
        }
    }

    private boolean isBypassToken(HttpServletRequest request) {
        final List<Pair<String, String>> bypass = Arrays.asList(
                Pair.of(apiPrefix + "/users/register", "POST"),
                Pair.of(apiPrefix + "/users/login", "POST"),
                Pair.of(apiPrefix + "/users/refreshToken",   "POST"),
                Pair.of(apiPrefix + "/roles**",              "GET"),
                Pair.of(apiPrefix + "/products**",           "GET"),
                Pair.of(apiPrefix + "/categories**",         "GET"),
                Pair.of(apiPrefix + "/comments**",           "GET"),
                Pair.of(apiPrefix + "/actuator/**",          "GET"),
                Pair.of(apiPrefix + "/coupons**", "GET"),
                Pair.of("/api-docs",                         "GET"),
                Pair.of("/swagger-ui/**",                    "GET")

        );

        String path = request.getServletPath();
        String method = request.getMethod();
        return bypass.stream().anyMatch(p -> path.matches(p.getFirst().replace("**", ".*"))
                && method.equalsIgnoreCase(p.getSecond()));
    }
}
