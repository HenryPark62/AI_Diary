package backend.User.service;

import backend.User.dto.OAuthUserInfo;
import backend.User.entity.TemporaryUser;
import backend.User.entity.User;
import backend.User.repository.TemporaryUserRepository;
import backend.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final TemporaryUserRepository temporaryUserRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = new DefaultOAuth2UserService().loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // "google", "kakao"

        OAuthUserInfo userInfo;
        if (registrationId.equals("google")) {
            userInfo = OAuthUserInfo.fromGoogle(oauth2User.getAttributes());
        } else {
            throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다: " + registrationId);
        }

        Optional<User> existingUser = userRepository.findByEmail(userInfo.getEmail());
        if (existingUser.isPresent()) {
            return oauth2User; // 기존 유저 로그인 완료
        }

        // 임시 유저로 등록 여부 확인
        if (temporaryUserRepository.findByEmail(userInfo.getEmail()).isEmpty()) {
            TemporaryUser tempUser = TemporaryUser.builder()
                    .email(userInfo.getEmail())
                    .nickname(userInfo.getNickname())
                    .password("") // 소셜 로그인은 비번 없음
                    .failedAttempts(0)
                    .build();

            temporaryUserRepository.save(tempUser);
        }

        // 이 시점에서 회원가입 진행 페이지로 리다이렉트하거나 안내 필요
        return oauth2User;
    }
}
