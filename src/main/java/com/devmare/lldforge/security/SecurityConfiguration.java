package com.devmare.lldforge.security;

import com.devmare.lldforge.business.dto.DefaultResponseDto;
import com.devmare.lldforge.data.enums.Role;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Map;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login/**", "/css/**", "/js/**").permitAll()
                        .requestMatchers("/admin/**").hasRole(Role.ADMIN.name())
                        .requestMatchers("/mentor/**").hasAnyRole(Role.ADMIN.name(), Role.MENTOR.name())
                        .requestMatchers("/student/**").hasAnyRole(Role.ADMIN.name(), Role.MENTOR.name(), Role.STUDENT.name())
                        .requestMatchers("/users/**").hasAnyRole(Role.ADMIN.name(), Role.MENTOR.name(), Role.STUDENT.name())
                        .anyRequest()
                        .authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                        .accessDeniedHandler(new CustomAccessDeniedHandler())
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oauth2UserService())
                        )
                        .defaultSuccessUrl("/users/me", true)
                        .failureHandler((request, response, exception) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            DefaultResponseDto body = new DefaultResponseDto(
                                    DefaultResponseDto.Status.FAILED,
                                    Map.of("error", "OAuth2 Login Failed - " + exception.getMessage()),
                                    "Login failed"
                            );
                            response.getWriter().write(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body));
                        })
                );
        return httpSecurity.build();
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        return customOAuth2UserService;
    }
}
