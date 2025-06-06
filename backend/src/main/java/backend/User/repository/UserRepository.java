package backend.User.repository;

import backend.User.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUserId(Long userId);

    Optional<User> findByNickname(String nickname);

    boolean existsByNickname(String nickname);

    boolean existsByEmail(String email);

    void deleteByEmail(String email);

    List<User> findByActiveFalseAndDeletionScheduledAtBefore(LocalDateTime dateTime);
}
