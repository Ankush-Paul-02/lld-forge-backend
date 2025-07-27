package com.devmare.lldforge.security;

import com.devmare.lldforge.business.dto.DefaultResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.devmare.lldforge.business.dto.DefaultResponseDto.Status.FAILED;

@Slf4j
@Component
public class AppAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            log.warn("User: {} attempted to access protected URL: {}", authentication.getName(), request.getRequestURI());
        }
        response.setStatus(403);
        new ObjectMapper().writeValue(response.getOutputStream(), new DefaultResponseDto(FAILED, "Access denied"));
    }
}
