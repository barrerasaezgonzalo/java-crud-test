package cl.gbarrera.demo.service;

import static cl.gbarrera.demo.util.Messages.USER_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cl.gbarrera.demo.user.application.domain.User;
import cl.gbarrera.demo.user.application.service.UserService;
import cl.gbarrera.demo.user.infrastructure.persistence.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserServiceTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private UserService userService;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testLoadUserByUsername() {
    UserRepository mockRepo = Mockito.mock(UserRepository.class);
    PasswordEncoder mockEncoder = Mockito.mock(PasswordEncoder.class);
    UserService service = new UserService(mockRepo, mockEncoder);
    User user = new User(1L, "john", "1234565");
    Mockito.when(mockRepo.findByUsername("john")).thenReturn(Optional.of(user));
    UserDetails result = service.loadUserByUsername("john");
    assertEquals("john", result.getUsername());
  }

  @Test
  public void testAuthenticateUser_Success() {
    UserRepository mockRepo = Mockito.mock(UserRepository.class);
    PasswordEncoder mockEncoder = Mockito.mock(PasswordEncoder.class);
    UserService service = new UserService(mockRepo, mockEncoder);
    String username = "john";
    String rawPassword = "123456";
    String encodedPassword = "$2a$10$abc...";
    User user = new User(1L, username, encodedPassword);
    Mockito.when(mockRepo.findByUsername(username)).thenReturn(Optional.of(user));
    Mockito.when(mockEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
    boolean result = service.authenticateUser(username, rawPassword);
    assertTrue(result);
  }

  @Test
  public void testLoadUserByUsername_UserNotFound() {
    UserRepository mockRepo = Mockito.mock(UserRepository.class);
    PasswordEncoder mockEncoder = Mockito.mock(PasswordEncoder.class);
    UserService service = new UserService(mockRepo, mockEncoder);
    String username = "john";
    Mockito.when(mockRepo.findByUsername(username)).thenReturn(Optional.empty());
    Exception exception =
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername(username));
    assertTrue(exception.getMessage().contains(USER_NOT_FOUND));
  }
}
