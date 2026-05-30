package com.carddemo.auth.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
                "TestSecretKeyThatIsAtLeast32BytesLongForHS256AlgorithmTesting!!",
                900000L,
                604800000L
        );
    }

    @Test
    void generateAccessToken_shouldReturnValidToken() {
        String token = jwtTokenProvider.generateAccessToken("user01");

        assertThat(token).isNotNull().isNotEmpty();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isEqualTo("user01");
    }

    @Test
    void generateRefreshToken_shouldReturnValidToken() {
        String token = jwtTokenProvider.generateRefreshToken("user01");

        assertThat(token).isNotNull().isNotEmpty();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isEqualTo("user01");
    }

    @Test
    void validateToken_withInvalidToken_shouldReturnFalse() {
        assertThat(jwtTokenProvider.validateToken("invalid.token.here")).isFalse();
    }

    @Test
    void validateToken_withEmptyToken_shouldReturnFalse() {
        assertThat(jwtTokenProvider.validateToken("")).isFalse();
    }

    @Test
    void validateToken_withExpiredToken_shouldReturnFalse() {
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(
                "TestSecretKeyThatIsAtLeast32BytesLongForHS256AlgorithmTesting!!",
                -1000L,
                -1000L
        );
        String token = shortLivedProvider.generateAccessToken("user01");

        assertThat(jwtTokenProvider.validateToken(token)).isFalse();
    }

    @Test
    void getUserIdFromToken_shouldExtractCorrectUserId() {
        String token = jwtTokenProvider.generateAccessToken("admin01");
        String userId = jwtTokenProvider.getUserIdFromToken(token);

        assertThat(userId).isEqualTo("admin01");
    }

    @Test
    void validateToken_withDifferentSecret_shouldReturnFalse() {
        JwtTokenProvider otherProvider = new JwtTokenProvider(
                "DifferentSecretKeyThatIsAtLeast32BytesLongForHS256Algorithm!!",
                900000L,
                604800000L
        );
        String token = otherProvider.generateAccessToken("user01");

        assertThat(jwtTokenProvider.validateToken(token)).isFalse();
    }
}
