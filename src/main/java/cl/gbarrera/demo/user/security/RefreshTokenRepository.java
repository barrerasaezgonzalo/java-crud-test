package cl.gbarrera.demo.user.security;

import jakarta.transaction.Transactional;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
  Optional<RefreshToken> findByToken(String token);

  @Transactional
  @Modifying
  @Query("DELETE FROM RefreshToken r WHERE r.username = :username")
  void deleteByUsername(@Param("username") String username);

  @Transactional
  @Modifying
  void deleteByToken(String token);
}
