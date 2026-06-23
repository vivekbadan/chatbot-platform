package com.chatbot.apigateway.filter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Collections;
import com.chatbot.apigateway.security.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Debug line
        System.out.println("PATH = " + path);

        // Allow Swagger and token endpoint
        if (path.startsWith("/swagger-ui")
                || path.startsWith("/swagger-ui.html")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/bridge/v3/api-docs")
                || path.startsWith("/organization/v3/api-docs")
                || path.equals("/token")
                || path.equals("/bridge/chat")) {

            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Missing JWT Token");

            return;
        }

        String token = authHeader.substring(7);

        try {

            JwtUtil.validateToken(token);
            System.out.println("JWT VALID");

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            "vivek",
                            null,
                            Collections.emptyList()
                    );

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

        } catch (JwtException e) {

            System.out.println("JWT INVALID");

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid JWT Token");

            return;
        }

        filterChain.doFilter(request, response);
    }
}