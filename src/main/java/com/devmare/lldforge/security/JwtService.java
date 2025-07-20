package com.devmare.lldforge.security;

import com.devmare.lldforge.business.dto.TokenPair;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class JwtService {

    private static final String TOKEN_PREFIX = "Bearer ";
    @Value("${app.jwt.secret}")
    private String JWT_SECRET_KEY;
    @Value("${app.jwt.expiration}")
    private long JWT_EXPIRATION_MS;
    @Value("${app.jwt.refresh-expiration}")
    private long JWT_REFRESH_EXPIRATION_MS;

    ///  Generate access token
    public String generateAccessToken(Authentication authentication) {
        return generateToken(authentication, JWT_EXPIRATION_MS, new HashMap<>());
    }

    ///  Generate refresh token
    public String generateRefreshToken(Authentication authentication) {
        Map<String, String> claims = new HashMap<>();
        claims.put("tokenType", "refresh");

        return generateToken(authentication, JWT_REFRESH_EXPIRATION_MS, claims);
    }

    ///  Validate token
    public boolean isValidToken(String token, UserDetails userDetails) {
        final String username = extractUsernameFromToken(token);
        if (!username.equals(userDetails.getUsername())) {
            log.error("Invalid token for user: {}", username);
            return false;
        }

        try {
            Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid token signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Token expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Jwt claims is empty: {}", e.getMessage());
        }
        return false;
    }

    /// Validate if the token is refersh token
    public boolean isRefreshToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("tokenType").equals("refresh");
    }

    ///  Extract username from token
    public String extractUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    ///  Generate token pair
    public TokenPair generateTokenPair(Authentication authentication) {
        String accessToken = generateAccessToken(authentication);
        String refreshToken = generateRefreshToken(authentication);
        return TokenPair.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(JWT_SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String generateToken(Authentication authentication, long expirationTime, Map<String, String> claims) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .header()
                .add("typ", "JWT")
                .and()
                .subject(userDetails.getUsername())
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSignInKey())
                .compact();
    }
}
