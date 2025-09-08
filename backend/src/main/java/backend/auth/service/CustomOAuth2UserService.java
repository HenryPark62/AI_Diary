package backend.auth.service;

import backend.auth.service.TemporaryUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final TemporaryUserService tempUserService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest req) {
        OAuth2User raw = super.loadUser(req);

        String reg = req.getClientRegistration().getRegistrationId(); // "google"
        String nameKey = req.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();
        if (nameKey == null || nameKey.isBlank()) nameKey = "sub";

        Map<String, Object> src = raw.getAttributes();
        Map<String, Object> attrs = new LinkedHashMap<>(src); // ← 가변 복사본

        String providerId = null, email = null, name = null;
        if ("google".equals(reg)) {
            providerId = String.valueOf(src.get("sub"));
            email      = (String) src.get("email");
            name       = (String) src.get("name");
        } else {
            throw new IllegalStateException("Unsupported provider: " + reg);
        }

        if (providerId == null || providerId.isBlank()) {
            throw new IllegalStateException("Missing providerId(sub) in OAuth attributes");
        }

        // 임시유저 + state 발급
        var tu = tempUserService.createOrRefresh(reg, providerId, email, name);

        // 발급된 state와 최소 메타만 주입 (불변 Map에 put 말 것!)
        attrs.put("provider", reg);
        attrs.put("providerId", providerId);
        if (email != null) attrs.put("email", email);
        if (name  != null) attrs.put("name", name);
        attrs.put("state", tu.getState());

        return new DefaultOAuth2User(raw.getAuthorities(), attrs, nameKey);
    }
}
