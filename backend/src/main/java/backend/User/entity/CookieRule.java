package backend.User.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CookieRule {
    ACCESS_TOKEN_NAME("access_token"),
    USER("user"),
    AUTHORIZATION("authorization"),

    JWT_ISSUE_HEADER("Set-Cookie"),
    JWT_RESOLVE_HEADER("Cookie"),
    REFRESH_TOKEN_NAME("refreshToken"),
    LANGUAGE_NAME("language");

    private final String value;
}
