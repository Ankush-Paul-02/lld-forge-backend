package com.devmare.lldforge.security;

import com.devmare.lldforge.business.dto.LoginRequestDto;
import com.devmare.lldforge.business.dto.RefreshTokenRequest;
import com.devmare.lldforge.business.dto.SignupUserRequestDto;
import com.devmare.lldforge.business.dto.TokenPair;
import com.devmare.lldforge.data.entity.User;
import com.devmare.lldforge.data.exception.AppInfoException;
import com.devmare.lldforge.data.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public void signupUser(SignupUserRequestDto request) {
        String password = request.getPassword();
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        if (!Pattern.matches(regex, password)) {
            log.error("Invalid password format");
            throw new AppInfoException("Password must be at least 8 characters long and include uppercase, lowercase, number, and special character", HttpStatus.BAD_REQUEST);
        }

        ///  Check if user with the same username already exists or not
        if (userRepository.existsByEmail(request.getEmail())) {
            log.error("User with email {} already exists", request.getEmail());
            throw new AppInfoException("User with email " + request.getEmail() + " already exists", HttpStatus.BAD_REQUEST);
        }

        /// Create new user
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        userRepository.save(user);
    }

    public TokenPair login(LoginRequestDto request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        /// Set authentication in security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ///  Generate Token
        return jwtService.generateTokenPair(authentication);
    }

    public User fetchAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();
        Optional<User> optionalUser = userRepository.findByEmail(currentUserName);
        if (optionalUser.isEmpty()) {
            log.error("User with email {} not found", currentUserName);
            throw new AppInfoException("User with email " + currentUserName + " not found", HttpStatus.NOT_FOUND);
        }
        return optionalUser.get();
    }

    public TokenPair refreshToken(@Valid RefreshTokenRequest request) {
        if (!jwtService.isRefreshToken(request.getRefreshToken())) {
            throw new AppInfoException("Invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        String username = jwtService.extractUsernameFromToken(request.getRefreshToken());
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (userDetails == null) {
            throw new AppInfoException("User not found", HttpStatus.NOT_FOUND);
        }

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
        String accessToken = jwtService.generateAccessToken(authToken);
        return TokenPair.builder()
                .accessToken(accessToken)
                .refreshToken(request.getRefreshToken())
                .build();
    }
}
