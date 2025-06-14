package cl.gbarrera.demo.user.infrastructure.web.controller;

import static cl.gbarrera.demo.util.Messages.*;

import cl.gbarrera.demo.infrastructure.web.dto.ErrorResponseDto;
import cl.gbarrera.demo.infrastructure.web.dto.TokenRefreshRequest;
import cl.gbarrera.demo.infrastructure.web.dto.TokenRefreshResponse;
import cl.gbarrera.demo.user.application.dto.UserDto;
import cl.gbarrera.demo.user.application.service.UserService;
import cl.gbarrera.demo.user.infrastructure.web.dto.LogoutRequest;
import cl.gbarrera.demo.user.security.JwtService;
import cl.gbarrera.demo.user.security.RefreshTokenService;
import com.auth0.jwt.exceptions.JWTVerificationException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

  private final UserService userService;
  private final JwtService jwtService;
  private final RefreshTokenService refreshTokenService;

  public UserController(
      UserService userService, JwtService jwtService, RefreshTokenService refreshTokenService) {
    this.userService = userService;
    this.jwtService = jwtService;
    this.refreshTokenService = refreshTokenService;
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody UserDto dto) {
    log.info("User: {} login in", dto.getUsername());
    boolean isValid = userService.authenticateUser(dto.getUsername(), dto.getPassword());
    if (isValid) {
      String accessToken = jwtService.generateAccessToken(dto.getUsername());
      String refreshToken = refreshTokenService.createRefreshToken(dto.getUsername());

      Map<String, Object> response = new HashMap<>();
      response.put("access_token", accessToken);
      response.put("refresh_token", refreshToken);
      response.put("token_type", "Bearer");
      response.put("expires_in", 3600);

      return ResponseEntity.ok(response);
    } else {
      ErrorResponseDto error =
          new ErrorResponseDto(
              INVALID_CREDENTIALS, INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED.value());
      log.info("User: {} login error: {}", dto.getUsername(), INVALID_CREDENTIALS);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
  }

  @PostMapping("/refresh")
  public ResponseEntity<?> refresh(@RequestBody TokenRefreshRequest request) {
    String oldRefreshToken = request.getRefreshToken();

    if (oldRefreshToken == null || oldRefreshToken.isEmpty()) {
      return ResponseEntity.badRequest().body(MISSING_REFRESH_TOKEN);
    }

    try {
      String username = refreshTokenService.verifyRefreshToken(oldRefreshToken);
      refreshTokenService.deleteByToken(oldRefreshToken);
      String newAccessToken = jwtService.generateAccessToken(username);
      String newRefreshToken = refreshTokenService.createRefreshToken(username);

      TokenRefreshResponse response = new TokenRefreshResponse(
              newAccessToken,
              newRefreshToken,
              "Bearer",
              900
      );

      return ResponseEntity.ok(response);

    } catch (JWTVerificationException ex) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(INVALID_REFRESH_TOKEN);
    }
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout(@RequestBody LogoutRequest request) {
    String refreshToken = request.getRefreshToken();
    refreshTokenService.deleteByToken(refreshToken);
    return ResponseEntity.ok("Logged out");
  }

}
