package cl.gbarrera.demo.controller;

import cl.gbarrera.demo.dto.ErrorResponseDto;
import cl.gbarrera.demo.dto.UserDto;
import cl.gbarrera.demo.service.JwtService;
import cl.gbarrera.demo.service.RefreshTokenService;
import cl.gbarrera.demo.service.UserService;

import java.util.HashMap;
import java.util.Map;

import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static cl.gbarrera.demo.util.Messages.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public UserController(UserService userService, JwtService jwtService, RefreshTokenService refreshTokenService) {
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
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> request) {
        String oldRefreshToken = request.get("refresh_token");

        if (oldRefreshToken == null || oldRefreshToken.isEmpty()) {
            return ResponseEntity.badRequest().body(MISSING_REFRESH_TOKEN);
        }

        try {
            String username = refreshTokenService.verifyRefreshToken(oldRefreshToken);
            refreshTokenService.deleteByToken(oldRefreshToken);
            String newAccessToken = jwtService.generateAccessToken(username);
            String newRefreshToken = refreshTokenService.createRefreshToken(username);

            Map<String, Object> response = new HashMap<>();
            response.put("access_token", newAccessToken);
            response.put("refresh_token", newRefreshToken);
            response.put("token_type", "Bearer");
            response.put("expires_in", 900);

            return ResponseEntity.ok(response);

        } catch (JWTVerificationException ex) {
            return ResponseEntity.status((HttpStatus.UNAUTHORIZED)).body(INVALID_REFRESH_TOKEN);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refresh_token");
        refreshTokenService.deleteByToken(refreshToken);
        return ResponseEntity.ok("Logged out");
    }
}
