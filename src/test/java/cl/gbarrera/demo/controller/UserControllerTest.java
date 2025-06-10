package cl.gbarrera.demo.controller;

import static cl.gbarrera.demo.util.Messages.INVALID_CREDENTIALS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import cl.gbarrera.demo.infrastructure.web.dto.ErrorResponseDto;
import cl.gbarrera.demo.infrastructure.web.dto.TokenRefreshRequest;
import cl.gbarrera.demo.infrastructure.web.dto.TokenRefreshResponse;
import cl.gbarrera.demo.user.application.dto.UserDto;
import cl.gbarrera.demo.user.application.service.UserService;
import cl.gbarrera.demo.user.infrastructure.web.controller.UserController;
import cl.gbarrera.demo.user.infrastructure.web.dto.LogoutRequest;
import cl.gbarrera.demo.user.security.JwtService;
import cl.gbarrera.demo.user.security.RefreshTokenService;
import com.auth0.jwt.exceptions.JWTVerificationException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

  @InjectMocks private UserController userController;

  @Mock private UserService userService;

  @Mock private JwtService jwtService;

  @Mock private RefreshTokenService refreshTokenService;

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

    TokenRefreshRequest request = new TokenRefreshRequest();
    request.setRefreshToken(oldRefreshToken);

    when(refreshTokenService.verifyRefreshToken(oldRefreshToken)).thenReturn(username);
    doNothing().when(refreshTokenService).deleteByToken(oldRefreshToken);
    when(jwtService.generateAccessToken(username)).thenReturn(newAccessToken);
    when(refreshTokenService.createRefreshToken(username)).thenReturn(newRefreshToken);

    ResponseEntity<?> response = userController.refresh(request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    TokenRefreshResponse body = (TokenRefreshResponse) response.getBody();
    assertEquals(newAccessToken, body.getAccessToken());
    assertEquals(newRefreshToken, body.getRefreshToken());
    assertEquals("Bearer", body.getTokenType());
    assertEquals(900, body.getExpiresIn());
  }

  @Test
  void refreshToken_shouldReturnBadRequest_whenRefreshTokenIsMissing() {
    TokenRefreshRequest request = new TokenRefreshRequest();

    ResponseEntity<?> response = userController.refresh(request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Missing refresh token", response.getBody());
  }

  @Test
  void refreshToken_shouldReturnUnauthorized_whenRefreshTokenIsInvalid() {
    TokenRefreshRequest request = new TokenRefreshRequest("invalid-token");

    when(refreshTokenService.verifyRefreshToken("invalid-token"))
            .thenThrow(new JWTVerificationException("Invalid token"));

    ResponseEntity<?> response = userController.refresh(request);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertEquals("Invalid refresh token", response.getBody());
  }


  @Test
  void logout_shouldReturnOkAndCallDeleteByToken() {
    String fakeRefreshToken = "some-refresh-token";

    LogoutRequest request = new LogoutRequest();
    request.setRefreshToken(fakeRefreshToken);

    doNothing().when(refreshTokenService).deleteByToken(fakeRefreshToken);

    ResponseEntity<?> response = userController.logout(request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Logged out", response.getBody());

    verify(refreshTokenService, times(1)).deleteByToken(fakeRefreshToken);
  }


  @Test
  void refreshToken_shouldReturnBadRequest_whenRefreshTokenIsEmpty() {
    TokenRefreshRequest request = new TokenRefreshRequest("");

    ResponseEntity<?> response = userController.refresh(request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Missing refresh token", response.getBody());
  }

}
