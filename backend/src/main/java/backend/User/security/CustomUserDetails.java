package backend.User.security;

import backend.User.entity.User;
import java.util.Collections;
import lombok.Getter;

@Getter
public class CustomUserDetails extends org.springframework.security.core.userdetails.User {
    private final Long userId;

    public CustomUserDetails(User user) {
        super(
                user.getEmail(),               // username 으로 이메일
                user.getPassword(),            // password
                Collections.emptyList()        // 권한 없음
        );
        this.userId = user.getUserId();
    }
}
