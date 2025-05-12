package cl.gbarrera.demo.controller;

import cl.gbarrera.demo.dto.UserDto;
import cl.gbarrera.demo.service.JwtService;
import cl.gbarrera.demo.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static cl.gbarrera.demo.util.Messages.INVALID_CREDENTIALS;
import static cl.gbarrera.demo.util.Messages.LOGIN_SUCCESSFUL;

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
        Optional<Boolean> isValid = userService.authenticateUser(dto);

        if (isValid.isPresent() && isValid.get()) {
            String token = jwtService.generateToken(dto.getUsername());
            return ResponseEntity.ok(token);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(INVALID_CREDENTIALS);
        }
    }
}
