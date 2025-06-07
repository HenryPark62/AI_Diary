package backend.User.security;

import backend.User.entity.CookieRule;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {

    // 쿠키 만료 시간 (초 단위, 여기서는 1일)
    private static final int ACCESS_TOKEN_EXPIRATION_SECONDS = 86400;

    private final JwtUtil jwtUtil;

    public void addAccessTokenCookie(HttpServletResponse response, String userEmail) {
        String token = jwtUtil.generateToken(userEmail);

        response.setHeader("Set-Cookie",
                "accessToken=" + token + "; Path=/; Max-Age=86400; HttpOnly; SameSite=None; Secure");

        System.out.println("JwtService.addAccessTokenCookie: Set-Cookie = accessToken=" + token
                + "; Path=/; Max-Age=86400; HttpOnly; SameSite=None; Secure");
    }

    public void clearAccessTokenCookie(HttpServletResponse response) {
        ResponseCookie expiredCookie = ResponseCookie.from(CookieRule.ACCESS_TOKEN_NAME.getValue(), "")
                .httpOnly(true)
                .secure(false) // 개발환경
                .path("/")
                .sameSite("None")
                .maxAge(0)
                .build();

        response.addHeader("Set-Cookie", expiredCookie.toString());
    }
}
