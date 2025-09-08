package backend.auth.security;

import backend.auth.entity.User;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Getter
public class CustomUserDetails extends org.springframework.security.core.userdetails.User {
    private final Long userId;
    private final String nickName;

    public CustomUserDetails(User user) {
        super(
                user.getEmail(), // 이메일을 username으로 사용
                "",
                List.of(new SimpleGrantedAuthority("USER"))
        );
        this.userId = user.getId();
        this.nickName = user.getNickName();
    }
}