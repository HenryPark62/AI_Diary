package backend.auth.security;

import backend.auth.entity.CookieRule;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    public String resolveTokenFromCookie(Cookie[] cookies, CookieRule cookieRule) {
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(cookieRule.getValue()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse("");
    }

    public void addJwtCookie(HttpServletResponse response, String name, String token, boolean secure) {
        ResponseCookie cookie = ResponseCookie.from(name, token)
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .sameSite("None")
                .maxAge(172800) // 2일
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void clearJwtCookie(HttpServletResponse response, String name, boolean secure) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .sameSite("None")
                .maxAge(0)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    // 임시 사용자 ID 쿠키 추가
    public void addTempUserCookie(HttpServletResponse response, String tempUserId, boolean secure) {
        ResponseCookie cookie = ResponseCookie.from(CookieRule.TEMP_USER_ID.getValue(), tempUserId)
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .sameSite("None")
                .maxAge(1800) // 30분
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    // 임시 사용자 ID 쿠키에서 값 읽기
    public String getTempUserIdFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            return resolveTokenFromCookie(request.getCookies(), CookieRule.TEMP_USER_ID);
        }
        return "";
    }

    // 임시 사용자 ID 쿠키 삭제
    public void clearTempUserCookie(HttpServletResponse response, boolean secure) {
        clearJwtCookie(response, CookieRule.TEMP_USER_ID.getValue(), secure);
    }
}