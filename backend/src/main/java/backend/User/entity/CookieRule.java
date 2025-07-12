package backend.User.entity;

public enum CookieRule {
    ACCESS_TOKEN_NAME("access_token"),
    USER("user"),
    AUTHORIZATION("authorization");

    private final String value;

    CookieRule(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
