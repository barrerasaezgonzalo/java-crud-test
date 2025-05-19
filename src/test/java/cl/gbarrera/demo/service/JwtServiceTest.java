package cl.gbarrera.demo.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    @Test
    public void testJwtServiceConstructor() {
        String fakeSecret = "my-test-secret";
        long fakeExpiration = 3600000L;
        JwtService jwtService = new JwtService(fakeSecret, fakeExpiration);
        assertNotNull(jwtService);
    }

    @Test
    public void testGenerateToken() {
        String secret = "test-secret";
        long expiration = 3600000L;
        JwtService jwtService = new JwtService(secret, expiration);
        String username = "john";
        String token = jwtService.generateToken(username);
        assertNotNull(token);
        Algorithm algorithm = Algorithm.HMAC256(secret);
        DecodedJWT decodedJWT = JWT.require(algorithm).build().verify(token);
        assertEquals(username, decodedJWT.getSubject());
        assertNotNull(decodedJWT.getIssuedAt());
        assertNotNull(decodedJWT.getExpiresAt());
    }


    @Test
    public void testValidateTokenAndRetrieveSubject_ValidToken() {
        String secret = "test-secret";
        long expiration = 3600000L;
        JwtService jwtService = new JwtService(secret, expiration);
        String username = "john";
        String token = jwtService.generateToken(username);
        String subject = jwtService.validateTokenAndRetrieveSubject(token);
        assertEquals(username, subject);
    }

    @Test
    public void testValidateTokenAndRetrieveSubject_InvalidToken() {
        String secret = "test-secret";
        long expiration = 3600000L;
        JwtService jwtService = new JwtService(secret, expiration);
        String invalidToken = "this.is.an.invalid.token";
        assertThrows(JWTVerificationException.class, () -> {
            jwtService.validateTokenAndRetrieveSubject(invalidToken);
        });
    }


}
