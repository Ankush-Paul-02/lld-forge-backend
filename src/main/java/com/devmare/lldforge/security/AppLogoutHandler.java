package com.devmare.lldforge.security;

import com.devmare.lldforge.business.dto.DefaultResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import java.io.IOException;

import static com.devmare.lldforge.business.dto.DefaultResponseDto.Status.SUCCESS;

@Slf4j
public class AppLogoutHandler implements LogoutHandler {

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        SecurityContextHolder.getContext().setAuthentication(null);
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {

                if (cookie.getName().equals("refresh")) {
                    // Set the cookie value to an empty string
                    cookie.setValue("");

                    // Set the max age to 0 to delete the cookie
                    cookie.setMaxAge(0);

                    // Set the path if you have set a custom one when the cookie was created
                    cookie.setPath("/");

                    // Set cookies properties
                    cookie.setHttpOnly(true);
                    cookie.setSecure(true);

                    // Add the cookie to the response to send it back to the client
                    response.addCookie(cookie);
                    response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, proxy-revalidate");

                    // Set the Pragma header to support HTTP 1.0 caches
                    response.setHeader("Pragma", "no-cache");

                    // Set the Expires header to a date in the past to prevent caching
                    response.setDateHeader("Expires", 0);
                }
            }
        }

        try {
            new ObjectMapper().writeValue(response.getOutputStream(), new DefaultResponseDto(
                    SUCCESS,
                    "Logged out successfully"
            ));
        } catch (IOException e) {
            log.error("Error writing response: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}
