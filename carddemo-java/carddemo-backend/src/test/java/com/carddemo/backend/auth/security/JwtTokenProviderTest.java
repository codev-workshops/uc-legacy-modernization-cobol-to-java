package com.carddemo.backend.auth.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
                "CardDemoDefaultSecretKeyForJWTTokenGeneration2024!", 86400000L);
    }

    @Test
    void generateToken_returnsValidToken() {
        String token = jwtTokenProvider.generateToken("USER0001", "U");

        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void validateToken_validToken_returnsTrue() {
        String token = jwtTokenProvider.generateToken("USER0001", "U");

        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void validateToken_invalidToken_returnsFalse() {
        assertFalse(jwtTokenProvider.validateToken("invalid.token.here"));
    }

    @Test
    void validateToken_nullToken_returnsFalse() {
        assertFalse(jwtTokenProvider.validateToken(null));
    }

    @Test
    void validateToken_expiredToken_returnsFalse() {
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(
                "CardDemoDefaultSecretKeyForJWTTokenGeneration2024!", -1000L);
        String token = shortLivedProvider.generateToken("USER0001", "U");

        assertFalse(jwtTokenProvider.validateToken(token));
    }

    @Test
    void getUserIdFromToken_returnsCorrectUserId() {
        String token = jwtTokenProvider.generateToken("ADMIN001", "A");

        assertEquals("ADMIN001", jwtTokenProvider.getUserIdFromToken(token));
    }

    @Test
    void getUserTypeFromToken_returnsCorrectUserType() {
        String token = jwtTokenProvider.generateToken("ADMIN001", "A");

        assertEquals("A", jwtTokenProvider.getUserTypeFromToken(token));
    }

    @Test
    void generateToken_differentUsers_differentTokens() {
        String token1 = jwtTokenProvider.generateToken("USER0001", "U");
        String token2 = jwtTokenProvider.generateToken("ADMIN001", "A");

        assertFalse(token1.equals(token2));
    }

    @Test
    void validateToken_malformedToken_returnsFalse() {
        assertFalse(jwtTokenProvider.validateToken("not-a-jwt"));
    }

    @Test
    void validateToken_emptyToken_returnsFalse() {
        assertFalse(jwtTokenProvider.validateToken(""));
    }
}
