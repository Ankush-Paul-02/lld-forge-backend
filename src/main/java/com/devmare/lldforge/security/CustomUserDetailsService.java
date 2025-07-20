package com.devmare.lldforge.security;

import com.devmare.lldforge.data.entity.User;
import com.devmare.lldforge.data.exception.AppInfoException;
import com.devmare.lldforge.data.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> optionalUser = userRepository.findByEmail(username);
        if (optionalUser.isEmpty()) {
            log.error("User not found with email: {}", username);
            throw new AppInfoException("User not found with email: " + username, HttpStatus.NOT_FOUND);
        }
        return optionalUser.get();
    }
}
