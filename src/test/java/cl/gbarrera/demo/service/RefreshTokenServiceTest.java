package cl.gbarrera.demo.service;

import cl.gbarrera.demo.user.security.RefreshToken;
import cl.gbarrera.demo.user.security.RefreshTokenRepository;
import cl.gbarrera.demo.user.security.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.Instant;
import java.util.Optional;

import static cl.gbarrera.demo.util.Messages.EXPIRED_REFRESH_TOKEN;
import static cl.gbarrera.demo.util.Messages.MISSING_REFRESH_TOKEN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        try {
            java.lang.reflect.Field field = RefreshTokenService.class.getDeclaredField("refreshTokenDurationMs");
            field.setAccessible(true);
            field.set(refreshTokenService, 1000L * 60 * 60 * 24); // 1 día en ms
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void createRefreshToken_shouldSaveAndReturnToken() {
        String username = "testuser";

        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);

        String token = refreshTokenService.createRefreshToken(username);

        assertNotNull(token);
        verify(refreshTokenRepository).save(tokenCaptor.capture());

        RefreshToken savedToken = tokenCaptor.getValue();
        assertEquals(username, savedToken.getUsername());
        assertEquals(token, savedToken.getToken());
        assertTrue(savedToken.getExpiryDate().isAfter(Instant.now()));
    }

    @Test
    void findByToken_shouldReturnOptionalToken() {
        String token = "token123";
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);

        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(refreshToken));

        Optional<RefreshToken> result = refreshTokenService.findByToken(token);

        assertTrue(result.isPresent());
        assertEquals(token, result.get().getToken());
    }

    @Test
    void isExpired_shouldReturnTrueIfExpired() {
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setExpiryDate(Instant.now().minusSeconds(10));

        assertTrue(refreshTokenService.isExpired(expiredToken));
    }

    @Test
    void isExpired_shouldReturnFalseIfNotExpired() {
        RefreshToken validToken = new RefreshToken();
        validToken.setExpiryDate(Instant.now().plusSeconds(60));

        assertFalse(refreshTokenService.isExpired(validToken));
    }

    @Test
    void verifyRefreshToken_shouldReturnUsernameIfValid() {
        String token = "validToken";
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setUsername("user1");
        refreshToken.setExpiryDate(Instant.now().plusSeconds(60));

        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(refreshToken));

        String username = refreshTokenService.verifyRefreshToken(token);

        assertEquals("user1", username);
    }

    @Test
    void verifyRefreshToken_shouldThrowIfNotFound() {
        String token = "invalidToken";

        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                refreshTokenService.verifyRefreshToken(token)
        );
        assertEquals(MISSING_REFRESH_TOKEN, exception.getMessage());
    }

    @Test
    void verifyRefreshToken_shouldThrowIfExpiredAndDelete() {
        String token = "expiredToken";
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setExpiryDate(Instant.now().minusSeconds(60));

        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(refreshToken));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                refreshTokenService.verifyRefreshToken(token)
        );
        assertEquals(EXPIRED_REFRESH_TOKEN, exception.getMessage());

        verify(refreshTokenRepository).delete(refreshToken);
    }

    @Test
    void deleteByUsername_shouldCallRepository() {
        String username = "userToDelete";

        refreshTokenService.deleteByUsername(username);

        verify(refreshTokenRepository).deleteByUsername(username);
    }

    @Test
    void deleteByToken_shouldCallRepository() {
        String token = "tokenToDelete";

        refreshTokenService.deleteByToken(token);

        verify(refreshTokenRepository).deleteByToken(token);
    }
}
