package backend.User.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class OAuthUserInfo {
    private String provider;       // "google", "kakao"
    private String providerId;     // sub (google) 또는 id (kakao)
    private String email;
    private String nickname;
    private String profileImage;

    public static OAuthUserInfo fromGoogle(Map<String, Object> attributes) {
        return OAuthUserInfo.builder()
                .provider("google")
                .providerId((String) attributes.get("sub"))
                .email((String) attributes.get("email"))
                .nickname((String) attributes.get("name")) // 또는 "given_name"
                .profileImage((String) attributes.get("picture"))
                .build();
    }

}
