package cl.gbarrera.demo.controller;

import cl.gbarrera.demo.dto.UserDto;
import cl.gbarrera.demo.dto.ErrorResponseDto;
import cl.gbarrera.demo.service.JwtService;
import cl.gbarrera.demo.service.RefreshTokenService;
import cl.gbarrera.demo.service.UserService;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import cl.gbarrera.demo.model.RefreshToken;

import java.util.HashMap;
import java.util.Map;

import static cl.gbarrera.demo.util.Messages.INVALID_CREDENTIALS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    private final String VALID_USERNAME = "user1";
    private final String VALID_PASSWORD = "password123";

    @Test
    void login_shouldReturnToken_whenCredentialsAreValid() {

        UserDto loginRequest = new UserDto(null, VALID_USERNAME, VALID_PASSWORD);

        when(userService.authenticateUser(VALID_USERNAME, VALID_PASSWORD)).thenReturn(true);
        when(jwtService.generateAccessToken(VALID_USERNAME)).thenReturn("fake-jwt-token");
        when(refreshTokenService.createRefreshToken(VALID_USERNAME)).thenReturn("fake-refresh-token");

        ResponseEntity<?> response = userController.login(loginRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("fake-jwt-token", body.get("access_token"));
        assertEquals("Bearer", body.get("token_type"));
        assertEquals(3600, body.get("expires_in"));
        assertEquals("fake-refresh-token", body.get("refresh_token"));

    }

    @Test
    void login_shouldReturnUnauthorized_whenAuthenticationFails() {

        UserDto loginRequest = new UserDto(null, VALID_USERNAME, "wrongpassword");

        when(userService.authenticateUser(VALID_USERNAME, "wrongpassword")).thenReturn(false);

        ResponseEntity<?> response = userController.login(loginRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        ErrorResponseDto error = (ErrorResponseDto) response.getBody();
        assertNotNull(error);
        assertEquals(INVALID_CREDENTIALS, error.getMessage());
        assertEquals(INVALID_CREDENTIALS, error.getError());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), error.getStatus());
    }

    @Test
    void refreshToken_shouldReturnNewTokens_whenRefreshTokenIsValid() {
        String oldRefreshToken = "old-refresh-token";
        String username = "testuser";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";

        Map<String, String> request = new HashMap<>();
        request.put("refresh_token", oldRefreshToken);

        when(refreshTokenService.verifyRefreshToken(oldRefreshToken)).thenReturn(username);
        doNothing().when(refreshTokenService).deleteByToken(oldRefreshToken);
        when(jwtService.generateAccessToken(username)).thenReturn(newAccessToken);
        when(refreshTokenService.createRefreshToken(username)).thenReturn(newRefreshToken);

        ResponseEntity<?> response = userController.refresh(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(newAccessToken, body.get("access_token"));
        assertEquals(newRefreshToken, body.get("refresh_token"));
        assertEquals("Bearer", body.get("token_type"));
        assertEquals(900, body.get("expires_in"));
    }

    @Test
    void refreshToken_shouldReturnBadRequest_whenRefreshTokenIsMissing() {
        Map<String, String> request = new HashMap<>(); // sin "refresh_token"

        ResponseEntity<?> response = userController.refresh(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Missing refresh token", response.getBody());
    }

    @Test
    void refreshToken_shouldReturnUnauthorized_whenRefreshTokenIsInvalid() {
        Map<String, String> request = new HashMap<>();
        request.put("refresh_token", "invalid-token");

        when(refreshTokenService.verifyRefreshToken("invalid-token"))
                .thenThrow(new JWTVerificationException("Invalid token"));

        ResponseEntity<?> response = userController.refresh(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid refresh token", response.getBody());
    }

    @Test
    void logout_shouldReturnOkAndCallDeleteByToken() {
        Map<String, String> request = new HashMap<>();
        String fakeRefreshToken = "some-refresh-token";
        request.put("refresh_token", fakeRefreshToken);

        doNothing().when(refreshTokenService).deleteByToken(fakeRefreshToken);

        ResponseEntity<?> response = userController.logout(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Logged out", response.getBody());

        verify(refreshTokenService, times(1)).deleteByToken(fakeRefreshToken);
    }
    @Test
    void refreshToken_shouldReturnBadRequest_whenRefreshTokenIsEmpty() {
        Map<String, String> request = new HashMap<>();
        request.put("refresh_token", "");

        ResponseEntity<?> response = userController.refresh(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Missing refresh token", response.getBody());
    }

}
