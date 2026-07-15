package com.example.employee_transport_system.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expirationMs;

    /**
     * Constructs JwtUtil with configurable secret and expiration.
     *
     * @param secretString the JWT signing secret (injected from jwt.secret property)
     * @param expirationMs the token expiration in milliseconds (injected from jwt.expiration)
     */
    public JwtUtil(@Value("${jwt.secret}") final String secretString,
                   @Value("${jwt.expiration}") final long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Generates a JWT token for the given username and role.
     *
     * @param username the subject (email)
     * @param role     the user role claim
     * @return a signed JWT string
     */
    public String generateToken(final String username, final String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extracts the username (subject) from a JWT token.
     *
     * @param token the JWT token
     * @return the subject claim
     */
    public String extractUsername(final String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}