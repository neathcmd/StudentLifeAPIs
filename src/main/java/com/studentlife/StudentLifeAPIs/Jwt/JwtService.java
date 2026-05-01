//package com.studentlife.StudentLifeAPIs.Jwt;
//
//import com.studentlife.StudentLifeAPIs.Entity.Users;
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.JwtException;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.security.Keys;
//import lombok.RequiredArgsConstructor;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Service;
//
//import javax.crypto.SecretKey;
//import java.nio.charset.StandardCharsets;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.function.Function;
//
//import static com.studentlife.StudentLifeAPIs.Exception.ErrorsExceptionFactory.unauthorized;
//
//@Service
//@RequiredArgsConstructor
//@SuppressWarnings("unused")
//public class JwtService {
//
//    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
//
//    /*
//     * =========================
//     * JWT CONFIG
//     * =========================
//     */
//    @Value("${spring.jwt-secret}")
//    private String jwtSecret;
//
//    @Value("${spring.access-token-expire}")
//    private long accessTokenExpired;
//
//    @Value("${spring.refresh-token-expire}")
//    private long refreshTokenExpired;
//
//    /*
//     * =========================
//     * SIGNING KEY
//     * =========================
//     */
//    private SecretKey getSignKey() {
//        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
//    }
//
//    /*
//     * =========================
//     * CLAIMS CORE
//     * =========================
//     */
//    private Claims extractAllClaims(String token) {
//        return Jwts.parser()
//                .verifyWith(getSignKey())
//                .build()
//                .parseSignedClaims(token)
//                .getPayload();
//    }
//
//    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
//        return resolver.apply(extractAllClaims(token));
//    }
//
//    /*
//     * =========================
//     * TOKEN GENERATION
//     * =========================
//     */
//    public String generateAccessToken(
//            String userId,
//            String email,
//            String username,
//            List<String> roles
//    ) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("userId", userId);
//        claims.put("email", email);
//        claims.put("type", "access");
//
//        if (roles != null && !roles.isEmpty()) {
//            claims.put("roles", roles);
//        }
//
//        return buildToken(claims, username, accessTokenExpired);
//    }
//
//    public String generateRefreshToken(String userId) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("userId", userId);
//        claims.put("type", "refresh");
//
//        return buildToken(claims, userId, refreshTokenExpired);
//    }
//
//    private String buildToken(
//            Map<String, Object> claims,
//            String subject,
//            long expiration
//    ) {
//        Date now = new Date();
//        Date expiry = new Date(now.getTime() + expiration);
//
//        return Jwts.builder()
//                .claims(claims)
//                .subject(subject)
//                .issuedAt(now)
//                .expiration(expiry)
//                .signWith(getSignKey(), Jwts.SIG.HS512)
//                .compact();
//    }
//
//    /*
//     * =========================
//     * CLAIM EXTRACTORS
//     * =========================
//     */
//    public String extractUsername(String token) {
//        return extractClaim(token, Claims::getSubject);
//    }
//
//    public String extractUserId(String token) {
//        return extractClaim(token, claims -> claims.get("userId", String.class));
//    }
//
//    public Date extractExpiration(String token) {
//        return extractClaim(token, Claims::getExpiration);
//    }
//
//    public String extractTokenType(String token) {
//        return extractClaim(token, claims -> claims.get("type", String.class));
//    }
//
//    /*
//     * =========================
//     * VALIDATION
//     * =========================
//     */
//    private boolean isTokenNotExpired(String token) {
//        return extractExpiration(token).after(new Date());
//    }
//
//    public boolean isTokenValid(String token, UserDetails userDetails) {
//        return extractUsername(token).equals(userDetails.getUsername())
//                && isTokenNotExpired(token); // no ! needed, warning gone
//    }
//
//    public boolean validateToken(String token) {
//        if (token == null || token.isBlank()) {
//            logger.debug("Token validation failed: token is null or blank");
//            return false;
//        }
//
//        try {
//            extractAllClaims(token);
//            return isTokenNotExpired(token); // no ! needed, warning gone
//        } catch (JwtException | IllegalArgumentException e) {
//            logger.debug("Token validation failed: {}", e.getMessage());
//            return false;
//        }
//    }
//
//    /*
//     * =========================
//     * CURRENT USER
//     * =========================
//     */
//    public Users getCurrentUser() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        if (authentication == null ||
//                !authentication.isAuthenticated() ||
//                authentication.getPrincipal().equals("anonymousUser")) {
//            throw unauthorized("User not authenticated");
//        }
//
//        return (Users) authentication.getPrincipal();
//    }
//}

package com.studentlife.StudentLifeAPIs.Jwt;

import com.studentlife.StudentLifeAPIs.Entity.Users;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.studentlife.StudentLifeAPIs.Exception.ErrorsExceptionFactory.unauthorized;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
    private static final long ACCESS_TOKEN_EXPIRY = 30L * 24 * 60 * 60 * 1000; // 30 days in ms

    /*
     * =========================
     * JWT CONFIG
     * =========================
     */
    @Value("${spring.jwt-secret}")
    private String jwtSecret;

    /*
     * =========================
     * SIGNING KEY
     * =========================
     */
    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /*
     * =========================
     * CLAIMS CORE
     * =========================
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    /*
     * =========================
     * TOKEN GENERATION
     * =========================
     */
    public String generateAccessToken(
            String userId,
            String email,
            String username,
            List<String> roles
    ) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("type", "access");

        if (roles != null && !roles.isEmpty()) {
            claims.put("roles", roles);
        }

        return buildToken(claims, username, ACCESS_TOKEN_EXPIRY);
    }

    private String buildToken(
            Map<String, Object> claims,
            String subject,
            long expiration
    ) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSignKey(), Jwts.SIG.HS512)
                .compact();
    }

    /*
     * =========================
     * CLAIM EXTRACTORS
     * =========================
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }

    /*
     * =========================
     * VALIDATION
     * =========================
     */
    private boolean isTokenNotExpired(String token) {
        return extractExpiration(token).after(new Date());
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername())
                && isTokenNotExpired(token);
    }

    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            logger.debug("Token validation failed: token is null or blank");
            return false;
        }

        try {
            extractAllClaims(token);
            return isTokenNotExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            logger.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /*
     * =========================
     * CURRENT USER
     * =========================
     */
    public Users getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            throw unauthorized("User not authenticated");
        }

        return (Users) authentication.getPrincipal();
    }
}
