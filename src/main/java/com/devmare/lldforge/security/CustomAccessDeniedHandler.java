package com.devmare.lldforge.security;

import com.devmare.lldforge.business.dto.DefaultResponseDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.util.Map;

import static com.devmare.lldforge.business.dto.DefaultResponseDto.Status.FAILED;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        DefaultResponseDto body = new DefaultResponseDto(
                FAILED,
                Map.of("error", "Forbidden - You do not have permission to access this resource"),
                "Access denied"
        );

        response.getWriter().write(convertToJson(body));
    }

    private String convertToJson(DefaultResponseDto body) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body);
        } catch (Exception e) {
            return "{\"status\":\"FAILED\",\"message\":\"Access denied\"}";
        }
    }
}
