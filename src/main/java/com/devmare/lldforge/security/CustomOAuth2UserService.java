package com.devmare.lldforge.security;

import com.devmare.lldforge.data.entity.User;
import com.devmare.lldforge.data.enums.Role;
import com.devmare.lldforge.data.exception.AppInfoException;
import com.devmare.lldforge.data.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Value("${lldforge.adminEmail}")
    private String adminEmail;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        try {
            OAuth2User oAuth2User = super.loadUser(userRequest);
            return processOAuth2User(oAuth2User);
        } catch (Exception e) {
            log.error("OAuth2 authentication failed: {}", e.getMessage(), e);
            throw new AppInfoException("OAuth2 authentication failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    private OAuth2User processOAuth2User(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String githubId = getAttribute(attributes, "id");
        String username = getAttribute(attributes, "login");
        String name = getAttribute(attributes, "name");
        String avatarUrl = getAttribute(attributes, "avatar_url");
        String profileUrl = getAttribute(attributes, "html_url");
        String email = getAttribute(attributes, "email");

        if (githubId == null || username == null) {
            throw new AppInfoException("Missing required GitHub attributes", HttpStatus.BAD_REQUEST);
        }

        User user;
        try {
            Optional<User> existingUser = userRepository.findByGithubId(githubId);

            user = existingUser.map(existing -> {
                log.info("Updating existing user: {}", username);
                existing.setUsername(username);
                existing.setName(name);
                existing.setAvatarUrl(avatarUrl);
                existing.setProfileUrl(profileUrl);
                existing.setEmail(email != null ? email : "");
                return userRepository.save(existing);
            }).orElseGet(() -> {
                log.info("Creating new user: {}", username);
                User newUser = User.builder()
                        .githubId(githubId)
                        .email(email != null ? email : "")
                        .username(username)
                        .name(name)
                        .avatarUrl(avatarUrl)
                        .profileUrl(profileUrl)
                        .joinedAt(Instant.now().getEpochSecond())
                        // Assign default role (e.g., STUDENT) here if needed
                        .role(email != null && email.equals(adminEmail) ? Role.ADMIN : Role.STUDENT)
                        .build();
                return userRepository.save(newUser);
            });

        } catch (Exception e) {
            log.error("Database error while processing OAuth2 user: {}", e.getMessage(), e);
            throw new AppInfoException("Database error while processing OAuth2 user: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Map role to Spring Security authority
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        return new DefaultOAuth2User(authorities, attributes, "login");
    }

    private String getAttribute(Map<String, Object> attributes, String key) {
        Object value = attributes.get(key);
        if (value == null) {
            throw new AppInfoException("Missing attribute: " + key, HttpStatus.BAD_REQUEST);
        }
        return String.valueOf(value);
    }

    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User oAuth2User)) {
            throw new AppInfoException("User not authenticated", HttpStatus.UNAUTHORIZED);
        }

        String githubId = Objects.requireNonNull(oAuth2User.getAttribute("id")).toString();

        return userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new AppInfoException("User not found", HttpStatus.NOT_FOUND));
    }
}
