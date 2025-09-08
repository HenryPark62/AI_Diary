package backend.auth.security;

import backend.auth.entity.CookieRule;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;

    public void addAccessTokenCookie(HttpServletResponse response, String userEmail) {
        String token = jwtUtil.generateToken(userEmail);

        // 기존 쿠키 먼저 삭제
        clearAccessTokenCookie(response);

        // CookieUtil 사용하여 일관성 있는 쿠키 설정
        cookieUtil.addJwtCookie(response,
                CookieRule.ACCESS_TOKEN_NAME.getValue(),
                token,
                true); // secure 설정

        System.out.println("새로운 JWT 토큰 쿠키 설정 완료: " + token.substring(0, 20) + "...");
    }

    public void clearAccessTokenCookie(HttpServletResponse response) {
        // CookieUtil 사용하여 일관성 있는 쿠키 삭제
        cookieUtil.clearJwtCookie(response,
                CookieRule.ACCESS_TOKEN_NAME.getValue(),
                true); // secure 설정

        System.out.println("JWT 쿠키 삭제 완료");
    }
}