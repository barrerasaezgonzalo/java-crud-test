package cl.gbarrera.demo.service;

import cl.gbarrera.demo.dto.UserDto;
import cl.gbarrera.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Service
public class UserService {

   private final UserRepository userRepository;
   private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }



    public Optional<Boolean> authenticateUser(UserDto dto) {
        return userRepository.findByUsername(dto.getUsername())
                .map(user -> {
                    return passwordEncoder.matches(dto.getPassword(), user.getPassword());
                });
}}
