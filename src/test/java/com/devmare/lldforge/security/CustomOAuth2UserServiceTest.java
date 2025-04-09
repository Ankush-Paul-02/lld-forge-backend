package com.devmare.lldforge.security;

import com.devmare.lldforge.data.entity.User;
import com.devmare.lldforge.data.exception.AppInfoException;
import com.devmare.lldforge.data.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    @Mock
    private UserRepository userRepository; // Dependency of the class, mocked

    @Mock
    private SecurityContext securityContext; // Mocked SecurityContext from Spring Security

    @Mock
    private Authentication authentication; // Mocked Authentication object

    @Mock
    private OAuth2User oAuth2User; // Mocked principal (user details from GitHub)

    @BeforeEach
    void setup() {
        // Set the mocked security context before every test
        SecurityContextHolder.setContext(securityContext);
    }

    // ✅ Happy path: authenticated user exists and is found in DB
    @Test
    void getCurrentAuthenticatedUser_shouldReturnUserIfValid() {
        // Setup: Simulate authenticated user with GitHub ID "123"
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("id")).thenReturn("123");

        // Setup: Mock user data returned from DB
        User user = new User();
        user.setGithubId("123");
        user.setUsername("ankan");

        when(userRepository.findByGithubId("123")).thenReturn(Optional.of(user));

        // Act
        User result = customOAuth2UserService.getCurrentAuthenticatedUser();

        // Assert: The user is correctly returned
        assertNotNull(result);
        assertEquals("ankan", result.getUsername());
    }

    // ❌ Case: No authentication found in the context
    @Test
    void getCurrentAuthenticatedUser_shouldThrowIfNotAuthenticated() {
        // Setup: No authentication in context
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act & Assert: Expect an exception due to missing authentication
        AppInfoException ex = assertThrows(AppInfoException.class, () -> {
            customOAuth2UserService.getCurrentAuthenticatedUser();
        });

        assertEquals("User not authenticated", ex.getMessage());
    }

    // ❌ Case: Authenticated, but GitHub ID is missing from attributes
    @Test
    void getCurrentAuthenticatedUser_shouldThrowIfGitHubIdMissing() {
        // Setup: User is authenticated but no "id" attribute
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("id")).thenReturn(null);

        // Act & Assert: Expect an exception due to missing GitHub ID
        AppInfoException ex = assertThrows(AppInfoException.class, () -> {
            customOAuth2UserService.getCurrentAuthenticatedUser();
        });

        assertEquals("GitHub ID is missing", ex.getMessage());
    }

    // ❌ Case: GitHub ID is provided, but no user found in DB
    @Test
    void getCurrentAuthenticatedUser_shouldThrowIfUserNotFound() {
        // Setup: User is authenticated and GitHub ID exists
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("id")).thenReturn("123");

        // Setup: DB returns empty
        when(userRepository.findByGithubId("123")).thenReturn(Optional.empty());

        // Act & Assert: Expect an exception since user is not in DB
        AppInfoException ex = assertThrows(AppInfoException.class, () -> {
            customOAuth2UserService.getCurrentAuthenticatedUser();
        });

        assertEquals("User not found", ex.getMessage());
    }
}
