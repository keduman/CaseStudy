package org.example.casestudy.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilsTest {

    @InjectMocks
    private JwtUtils jwtUtils;

    private Key secretKey;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Initialize the secretKey as JwtUtils does in @PostConstruct
        this.secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        ReflectionTestUtils.setField(jwtUtils, "secretKey", secretKey);
    }

    @Test
    void extractUsername_Success() {
        String username = "testuser";
        String token = jwtUtils.generateToken(username);

        String extractedUsername = jwtUtils.extractUsername(token);

        assertEquals(username, extractedUsername);
    }

    @Test
    void extractExpiration_Success() {
        String username = "testuser";
        String token = jwtUtils.generateToken(username);

        Date expiration = jwtUtils.extractExpiration(token);

        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void extractClaim_Success() {
        String username = "testuser";
        String token = jwtUtils.generateToken(username);

        String extractedUsername = jwtUtils.extractClaim(token, Claims::getSubject);

        assertEquals(username, extractedUsername);
    }

    @Test
    void validateToken_ValidToken() {
        String username = "testuser";
        String token = jwtUtils.generateToken(username);

        boolean isValid = jwtUtils.validateToken(token, username);

        assertTrue(isValid);
    }

    @Test
    void validateToken_InvalidUsername() {
        String username = "testuser";
        String token = jwtUtils.generateToken(username);
        String wrongUsername = "wronguser";

        boolean isValid = jwtUtils.validateToken(token, wrongUsername);

        assertFalse(isValid);
    }

    @Test
    void generateToken_Success() {
        String username = "testuser";
        String token = jwtUtils.generateToken(username);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }
}