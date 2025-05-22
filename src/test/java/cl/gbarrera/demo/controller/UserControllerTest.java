package cl.gbarrera.demo.controller;

import cl.gbarrera.demo.dto.UserDto;
import cl.gbarrera.demo.dto.ErrorResponseDto;
import cl.gbarrera.demo.service.JwtService;
import cl.gbarrera.demo.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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

    private final String VALID_USERNAME = "user1";
    private final String VALID_PASSWORD = "password123";

    @Test
    void login_shouldReturnToken_whenCredentialsAreValid() {

        UserDto loginRequest = new UserDto(null, VALID_USERNAME, VALID_PASSWORD);

        when(userService.authenticateUser(VALID_USERNAME, VALID_PASSWORD)).thenReturn(true);
        when(jwtService.generateToken(VALID_USERNAME)).thenReturn("fake-jwt-token");


        ResponseEntity<?> response = userController.login(loginRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("fake-jwt-token", body.get("access_token"));
        assertEquals("Bearer", body.get("token_type"));
        assertEquals(3600, body.get("expires_in"));
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
}
