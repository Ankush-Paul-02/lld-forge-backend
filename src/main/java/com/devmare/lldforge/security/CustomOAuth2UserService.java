package com.devmare.lldforge.security;

import com.devmare.lldforge.data.entity.User;
import com.devmare.lldforge.data.exception.AppInfoException;
import com.devmare.lldforge.data.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * Custom OAuth2 user service that handles authentication and user registration/update.
 * This service is responsible for:
 * - Fetching OAuth2 user details
 * - Checking if the user already exists in the system
 * - Updating or creating user records in the database
 * - Handling errors gracefully and logging issues
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Value("${lldforge.adminEmail}")
    private String adminEmail;

    /**
     * Loads user details from the OAuth2 provider and processes authentication.
     *
     * @param userRequest OAuth2 user request containing user information
     * @return an OAuth2User containing user details
     * @throws AppInfoException if authentication fails
     */
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

    /**
     * Processes the authenticated OAuth2 user.
     * - Extracts user attributes from OAuth2 provider (e.g., GitHub)
     * - Checks if the user already exists in the database
     * - Updates existing user or creates a new user if not found
     *
     * @param oAuth2User OAuth2 user information
     * @return processed OAuth2User
     * @throws AppInfoException if any critical attributes are missing or if there is a database error
     */
    private OAuth2User processOAuth2User(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // Extract required attributes from OAuth2 response
        String githubId = getAttribute(attributes, "id");
        String username = getAttribute(attributes, "login");
        String name = getAttribute(attributes, "name");
        String avatarUrl = getAttribute(attributes, "avatar_url");
        String profileUrl = getAttribute(attributes, "html_url");

        // Ensure mandatory attributes are present
        if (githubId == null || username == null) {
            throw new AppInfoException("Missing required GitHub attributes", HttpStatus.BAD_REQUEST);
        }

        try {
            Optional<User> existingUser = userRepository.findByGithubId(githubId);

            // Update existing user or create a new one
            existingUser.map(existing -> {
                log.info("Updating existing user: {}", username);
                existing.setUsername(username);
                existing.setName(name);
                existing.setAvatarUrl(avatarUrl);
                existing.setProfileUrl(profileUrl);
                return userRepository.save(existing);
            }).orElseGet(() -> {
                log.info("Creating new user: {}", username);
                User newUser = User.builder()
                        .githubId(githubId)
                        .username(username)
                        .name(name)
                        .avatarUrl(avatarUrl)
                        .profileUrl(profileUrl)
                        .joinedAt(Instant.now().getEpochSecond())
                        .build();
                return userRepository.save(newUser);
            });

            return oAuth2User;
        } catch (Exception e) {
            log.error("Database error while processing OAuth2 user: {}", e.getMessage(), e);
            throw new AppInfoException("Database error while processing OAuth2 user: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves an attribute from the OAuth2 user attributes map.
     * - Ensures the attribute exists before accessing it
     * - Throws an exception if the attribute is missing
     *
     * @param attributes Map containing OAuth2 user attributes
     * @param key        Attribute key to retrieve
     * @return String value of the attribute
     * @throws AppInfoException if the attribute is missing
     */
    private String getAttribute(Map<String, Object> attributes, String key) {
        Object value = attributes.get(key);
        if (value == null) {
            throw new AppInfoException("Missing attribute: " + key, HttpStatus.BAD_REQUEST);
        }
        return value.toString();
    }

    /**
     * Retrieves the currently authenticated user from the security context.
     *
     * @return The authenticated {@link User} entity from the database.
     * @throws AppInfoException if the user is not authenticated, GitHub ID is missing, or the user is not found in the database.
     */
    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User oAuth2User)) {
            throw new AppInfoException("User not authenticated", HttpStatus.UNAUTHORIZED);
        }

        String githubId = oAuth2User.getAttribute("id");

        if (githubId == null) {
            throw new AppInfoException("GitHub ID is missing", HttpStatus.BAD_REQUEST);
        }

        Optional<User> optionalUser = userRepository.findByGithubId(githubId);
        if (optionalUser.isEmpty()) {
            throw new AppInfoException("User not found", HttpStatus.NOT_FOUND);
        }
        return optionalUser.get();
    }
}
