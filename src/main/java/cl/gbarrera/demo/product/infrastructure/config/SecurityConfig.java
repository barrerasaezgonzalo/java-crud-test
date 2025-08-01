package cl.gbarrera.demo.product.infrastructure.config;

import cl.gbarrera.demo.user.security.JwtAuthenticationFilter;
import cl.gbarrera.demo.user.security.JwtService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;

  public SecurityConfig(JwtService jwtService, @Lazy UserDetailsService userDetailsService) {
    this.jwtService = jwtService;
    this.userDetailsService = userDetailsService;
  }

  @Bean
  public JwtAuthenticationFilter jwtAuthenticationFilter() {

    return new JwtAuthenticationFilter(jwtService, userDetailsService);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {

    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/users/login",
                        "/users/refresh",
                        "/users/logout",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/products/**")
                    .hasAuthority("ROLE_USER")
                    .requestMatchers(HttpMethod.POST, "/products/**")
                    .hasAuthority("ROLE_USER")
                    .requestMatchers(HttpMethod.PUT, "/products/**")
                    .hasAuthority("ROLE_USER")
                    .anyRequest()
                    .authenticated()
            /*  .anyRequest().permitAll() */
            )
        .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
    mapper.registerModule(new JavaTimeModule());
    return mapper;
  }
}
