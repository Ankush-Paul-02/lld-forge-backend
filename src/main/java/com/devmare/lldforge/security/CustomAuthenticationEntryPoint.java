package com.devmare.lldforge.security;

import com.devmare.lldforge.business.dto.DefaultResponseDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.util.Map;

import static com.devmare.lldforge.business.dto.DefaultResponseDto.Status.FAILED;

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        DefaultResponseDto body = new DefaultResponseDto(
                FAILED,
                Map.of("error", "Unauthorized - Please login first"),
                "Authentication failed"
        );

        response.getWriter().write(convertToJson(body));
    }

    private String convertToJson(DefaultResponseDto body) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body);
        } catch (Exception e) {
            return "{\"status\":\"FAILED\",\"message\":\"Authentication failed\"}";
        }
    }
}
