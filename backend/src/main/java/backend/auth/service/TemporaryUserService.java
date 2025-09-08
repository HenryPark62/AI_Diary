package backend.auth.service;

import backend.auth.entity.TemporaryUser;
import backend.auth.repository.TemporaryUserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TemporaryUserService {

    private final TemporaryUserRepository temporaryUserRepository;

    public TemporaryUser createOrRefresh(String provider, String providerId,
                                         String email, String name) {
        var tu = temporaryUserRepository
                .findByProviderAndProviderId(provider, providerId)
                .orElseGet(TemporaryUser::new);

        if (tu.getId() == null) {
            tu.setProvider(provider);
            tu.setProviderId(providerId);
        }
        tu.setEmail(email);
        tu.setName(name);

        // 새 state 발급(프론트 리다이렉트 키)
        tu.setState(RandomStringUtils.randomAlphanumeric(40));
        // 30분 후 만료
        tu.setExpiresAt(LocalDateTime.now().plusMinutes(30));

        return temporaryUserRepository.save(tu);
    }

    public TemporaryUser getByStateOrThrow(String state) {
        return temporaryUserRepository.findByState(state)
                .filter(t -> !t.isExpired())
                .orElseThrow(() -> new IllegalStateException("임시유저 만료 또는 존재하지 않음"));
    }

    public void deleteByState(String state) {
        temporaryUserRepository.deleteByState(state);
    }
}
