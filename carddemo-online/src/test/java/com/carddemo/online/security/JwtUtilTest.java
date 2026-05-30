package com.carddemo.online.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(
                "TestSecretKeyThatIsAtLeast32BytesLongForHMAC!!", 3600000L);
    }

    @Test
    void generateAndParseToken() {
        String token = jwtUtil.generateToken("ADMIN01", "A");
        assertNotNull(token);
        assertTrue(jwtUtil.isValid(token));
        assertEquals("ADMIN01", jwtUtil.getUserId(token));
        assertEquals("A", jwtUtil.getUserType(token));
    }

    @Test
    void isValid_invalidToken_returnsFalse() {
        assertFalse(jwtUtil.isValid("not.a.token"));
    }

    @Test
    void isValid_nullToken_returnsFalse() {
        assertFalse(jwtUtil.isValid(null));
    }

    @Test
    void expiredToken_isInvalid() {
        JwtUtil shortLived = new JwtUtil(
                "TestSecretKeyThatIsAtLeast32BytesLongForHMAC!!", 0L);
        String token = shortLived.generateToken("USR01", "U");
        assertFalse(shortLived.isValid(token));
    }

    @Test
    void generateToken_userType() {
        String token = jwtUtil.generateToken("USR01", "U");
        assertEquals("U", jwtUtil.getUserType(token));
    }
}
