    package cl.gbarrera.demo.service;

    import cl.gbarrera.demo.model.RefreshToken;
    import cl.gbarrera.demo.repository.RefreshTokenRepository;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.stereotype.Service;

    import java.time.Instant;
    import java.util.Optional;
    import java.util.UUID;

    import static cl.gbarrera.demo.util.Messages.EXPIRED_REFRESH_TOKEN;
    import static cl.gbarrera.demo.util.Messages.MISSING_REFRESH_TOKEN;

    @Service
    public class RefreshTokenService {

        @Value("${jwt.refresh-token.expiration}")
        private Long refreshTokenDurationMs;

        private final RefreshTokenRepository refreshTokenRepository;

        public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
            this.refreshTokenRepository = refreshTokenRepository;
        }

        public String createRefreshToken(String username) {
            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setUsername(username);
            refreshToken.setToken(UUID.randomUUID().toString());
            refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));

            refreshTokenRepository.save(refreshToken);

            return refreshToken.getToken();
        }

        public Optional<RefreshToken> findByToken(String token) {
            return refreshTokenRepository.findByToken(token);
        }

        public boolean isExpired(RefreshToken token) {
            return token.getExpiryDate().isBefore(Instant.now());
        }

        public void deleteByUsername(String username) {
            refreshTokenRepository.deleteByUsername(username);
        }

        public String verifyRefreshToken(String token) {
            RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                    .orElseThrow(() -> new RuntimeException(MISSING_REFRESH_TOKEN));

            if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
                refreshTokenRepository.delete(refreshToken);
                throw new RuntimeException(EXPIRED_REFRESH_TOKEN);
            }

            return refreshToken.getUsername();
        }

        public void deleteByToken(String token) {
            refreshTokenRepository.deleteByToken(token);
        }
    }
