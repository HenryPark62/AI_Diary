package backend.User.repository;

import backend.User.entity.TemporaryUser;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemporaryUserRepository extends JpaRepository<TemporaryUser, Long> {
    Optional<TemporaryUser> findByEmail(String email); // 이메일로 임시 사용자 조회

    void deleteByTemporaryUserId(Long temporaryUserId);

    boolean existsByEmail(String email);

    List<TemporaryUser> findByCreatedAtBefore(LocalDateTime dateTime);
}
