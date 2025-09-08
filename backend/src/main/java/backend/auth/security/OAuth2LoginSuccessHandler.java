package backend.auth.security;

import backend.auth.security.CookieUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final CookieUtil cookieUtil;

    // 소셜 로그인 성공 후, 프론트의 회원가입 완료 페이지로 이동
    // 예: https://frontend.example.com/signup/start?state=...
    private final String signupStartUrl = "https://frontend.example.com/signup/start"; // 실제 도메인으로 변경

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        Object principal = authentication.getPrincipal();
        String state = null;
        if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User oAuth2User) {
            Object s = oAuth2User.getAttributes().get("state");
            if (s != null) state = s.toString();
        }

        if (state == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "state 누락");
            return;
        }

        // 선택: 프론트와 안전한 교환을 위해 쿠키로 state 전달 (SameSite=None; Secure 여부는 환경에 따라)
        cookieUtil.addJwtCookie(response, "signupState", state, true);

        // 프론트로 리다이렉트 (쿼리에도 state 포함)
        response.sendRedirect(signupStartUrl + "?state=" + state);
    }
}