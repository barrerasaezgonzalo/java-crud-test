package cl.gbarrera.demo.user.application.service;

import cl.gbarrera.demo.user.infrastructure.persistence.UserRepository;
import cl.gbarrera.demo.user.application.domain.User;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static cl.gbarrera.demo.util.Messages.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND + ": " + username));

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), user.getPassword(), authorities );
    }

    public boolean authenticateUser(String username, String password) {
        return userRepository
                .findByUsername(username)
                .map(user -> passwordEncoder.matches(password, user.getPassword()))
                .orElse(false);
    }
}
