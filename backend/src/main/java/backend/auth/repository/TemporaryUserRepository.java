package backend.auth.repository;

import backend.auth.entity.TemporaryUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TemporaryUserRepository extends JpaRepository<TemporaryUser, Long> {
    Optional<TemporaryUser> findByState(String state);
    Optional<TemporaryUser> findByProviderAndProviderId(String provider, String providerId);
    void deleteByState(String state);
}
