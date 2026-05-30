package com.mainframe.carddemo.migration.job;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHashProcessorTest {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void shouldHashPassword() {
        String plaintext = "password";
        String hashed = encoder.encode(plaintext);
        assertNotEquals(plaintext, hashed);
        assertTrue(hashed.startsWith("$2a$"));
        assertTrue(encoder.matches(plaintext, hashed));
    }

    @Test
    void shouldProduceDifferentHashesForSameInput() {
        String plaintext = "test1234";
        String hash1 = encoder.encode(plaintext);
        String hash2 = encoder.encode(plaintext);
        assertNotEquals(hash1, hash2);
        assertTrue(encoder.matches(plaintext, hash1));
        assertTrue(encoder.matches(plaintext, hash2));
    }

    @Test
    void shouldHashEmptyPassword() {
        String hashed = encoder.encode("");
        assertTrue(encoder.matches("", hashed));
    }

    @Test
    void shouldHashCobolStylePassword() {
        // COBOL passwords are typically 8 chars, padded
        String cobolPwd = "SECRET  ";
        String hashed = encoder.encode(cobolPwd.trim());
        assertTrue(encoder.matches("SECRET", hashed));
    }
}
