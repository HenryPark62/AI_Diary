package backend.auth.repository;

import backend.auth.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByNickName(String nickName);

    boolean existsByEmail(String email);

    boolean existsByNickName(String nickName);
}
