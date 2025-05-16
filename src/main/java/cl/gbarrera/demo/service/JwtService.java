package cl.gbarrera.demo.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final String secret;
  private final long expirationMs;

  public JwtService(
      @Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") long expirationMs) {
    this.secret = secret;
    this.expirationMs = expirationMs;
  }

  public String generateToken(String username) {
    Algorithm algorithm = Algorithm.HMAC256(secret);
    return JWT.create()
        .withSubject(username)
        .withIssuedAt(new Date())
        .withExpiresAt(new Date(System.currentTimeMillis() + expirationMs))
        .sign(algorithm);
  }

  public String validateTokenAndRetrieveSubject(String token) {
    Algorithm algorithm = Algorithm.HMAC256(secret);
    JWTVerifier verifier = JWT.require(algorithm).acceptExpiresAt(0).build();
    DecodedJWT jwt = verifier.verify(token);
    return jwt.getSubject();
  }
}
