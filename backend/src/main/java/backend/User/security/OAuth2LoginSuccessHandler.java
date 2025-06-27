package backend.User.security;

import backend.User.entity.User;
import backend.User.repository.TemporaryUserRepository;
import backend.User.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final TemporaryUserRepository temporaryUserRepository;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        String email = ((DefaultOAuth2User) authentication.getPrincipal()).getAttribute("email");

        if (userRepository.findByEmail(email).isPresent()) {
            // 기존 회원이면 JWT 발급 후 메인 페이지로 이동
            String token = jwtUtil.generateToken(email);
            cookieUtil.addJwtCookie(response, "Authorization", token, true);
            response.sendRedirect("/"); // or 프론트 홈
        } else if (temporaryUserRepository.findByEmail(email).isPresent()) {
            // 임시 회원이면 회원가입 계속 페이지로 이동
            response.sendRedirect("/signup/continue");
        } else {
            // 새로운 소셜 유저면 임시회원 등록 후 회원가입 시작 페이지로 리다이렉트
            response.sendRedirect("/signup/start");
        }
    }
}
