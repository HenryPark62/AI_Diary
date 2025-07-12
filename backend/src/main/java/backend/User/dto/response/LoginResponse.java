package backend.User.dto.response;

import backend.User.entity.Gender;
import backend.User.entity.MBTI;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private String nickname;
    private String email;
    private MBTI mbti;
    private Gender gender;
    private List<String> hobbies;
    private List<String> favoriteFoods;
}
