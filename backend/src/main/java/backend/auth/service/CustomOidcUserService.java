package backend.auth.service;

import backend.auth.service.TemporaryUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final TemporaryUserService tempUserService;

    @Override
    public OidcUser loadUser(OidcUserRequest req) {
        log.info("[OIDC] enter loadUser");
        OidcUser raw = super.loadUser(req);

        // 가변 Claims 복사
        Map<String, Object> claimsSrc = raw.getClaims();
        Map<String, Object> claims = new LinkedHashMap<>(claimsSrc);

        // 구글 파싱
        String provider = req.getClientRegistration().getRegistrationId(); // "google"
        String sub      = (String) claims.get("sub");
        String email    = (String) claims.get("email");
        String name     = (String) claims.get("name");

        if (sub == null || sub.isBlank()) {
            throw new IllegalStateException("Missing sub in OIDC claims");
        }

        // 임시유저 + state 발급
        var tu = tempUserService.createOrRefresh(provider, sub, email, name);

        // attributes(claims)에 우리가 쓸 최소 메타 주입
        claims.put("provider", provider);
        claims.put("providerId", sub);
        if (email != null) claims.put("email", email);
        if (name  != null) claims.put("name", name);
        claims.put("state", tu.getState());

        // DefaultOidcUser로 재구성 (getAttributes()가 claims를 돌려주도록)
        Collection<? extends GrantedAuthority> authorities = raw.getAuthorities();
        OidcIdToken idToken = raw.getIdToken();
        OidcUserInfo userInfo = new OidcUserInfo(claims);

        // nameAttributeKey는 구글이면 "sub" 권장
        return new DefaultOidcUser(authorities, idToken, userInfo, "sub");
    }
}
