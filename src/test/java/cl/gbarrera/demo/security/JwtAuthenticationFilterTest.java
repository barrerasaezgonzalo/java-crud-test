package cl.gbarrera.demo.security;

import static cl.gbarrera.demo.util.Messages.INVALID_OR_EXPIRED_TOKEN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import cl.gbarrera.demo.user.security.JwtAuthenticationFilter;
import cl.gbarrera.demo.user.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public class JwtAuthenticationFilterTest {

  private JwtService jwtService;
  private UserDetailsService userDetailsService;
  private JwtAuthenticationFilter filter;
  private FilterChain filterChain;

  @BeforeEach
  void setUp() {
    jwtService = mock(JwtService.class);
    userDetailsService = mock(UserDetailsService.class);
    filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
    filterChain = mock(FilterChain.class);
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldCallNextFilterIfNoAuthorizationHeader() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  void shouldReturnUnauthorizedIfTokenInvalid() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer invalidtoken");
    MockHttpServletResponse response = new MockHttpServletResponse();

    when(jwtService.validateTokenAndRetrieveSubject("invalidtoken"))
        .thenThrow(new RuntimeException("Token inválido"));

    filter.doFilter(request, response, filterChain);

    assertEquals(401, response.getStatus());
    assertEquals("{\"error\":\"" + INVALID_OR_EXPIRED_TOKEN + "\"}", response.getContentAsString());

    verify(filterChain, never()).doFilter(any(), any());
  }

  @Test
  void shouldAuthenticateUserIfTokenValidAndNoAuthPresent() throws Exception {
    String token = "valid.token.here";
    String username = "gonza";

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer " + token);
    MockHttpServletResponse response = new MockHttpServletResponse();

    when(jwtService.validateTokenAndRetrieveSubject(token)).thenReturn(username);

    UserDetails userDetails = new User(username, "password", Collections.emptyList());
    when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

    filter.doFilter(request, response, filterChain);

    assertInstanceOf(
        UsernamePasswordAuthenticationToken.class,
        SecurityContextHolder.getContext().getAuthentication());
    assertEquals(username, SecurityContextHolder.getContext().getAuthentication().getName());

    verify(filterChain).doFilter(request, response);
  }

  @Test
  void shouldSkipFilterIfAuthorizationHeaderIsMalformed() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Token abcdef");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  void shouldSkipSettingAuthenticationIfAlreadyPresent() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer validtoken");
    MockHttpServletResponse response = new MockHttpServletResponse();

    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken("existingUser", null, List.of()));

    when(jwtService.validateTokenAndRetrieveSubject("validtoken")).thenReturn("testuser");

    filter.doFilter(request, response, filterChain);

    assertEquals(
        "existingUser", SecurityContextHolder.getContext().getAuthentication().getPrincipal());

    verify(filterChain).doFilter(request, response);

    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldSkipSettingAuthenticationIfUsernameIsNull() throws Exception {
    String token = "validtoken";

    when(jwtService.validateTokenAndRetrieveSubject(token)).thenReturn(null);

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer " + token);
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, filterChain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(filterChain).doFilter(request, response);
  }
}
