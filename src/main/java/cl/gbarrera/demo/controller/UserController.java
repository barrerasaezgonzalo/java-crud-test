package cl.gbarrera.demo.controller;

import static cl.gbarrera.demo.util.Messages.INVALID_CREDENTIALS;

import cl.gbarrera.demo.dto.ErrorResponseDto;
import cl.gbarrera.demo.dto.UserDto;
import cl.gbarrera.demo.service.JwtService;
import cl.gbarrera.demo.service.UserService;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDto dto) {
        boolean isValid = userService.authenticateUser(dto.getUsername(), dto.getPassword());
        if (isValid) {
            String token = jwtService.generateToken(dto.getUsername());
            Map<String, Object> response = new HashMap<>();
            response.put("access_token", token);
            response.put("token_type", "Bearer");
            response.put("expires_in", 3600);

            return ResponseEntity.ok(response);
        } else {
            ErrorResponseDto error =
                    new ErrorResponseDto(
                            INVALID_CREDENTIALS, INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED.value());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
}
