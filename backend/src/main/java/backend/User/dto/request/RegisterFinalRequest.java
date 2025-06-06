package backend.User.dto.request;

import backend.User.entity.Gender;
import backend.User.entity.MBTI;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterFinalRequest {
//    최종 확인용
    private String email;
    private MBTI mbti;
    private Gender gender;
    private List<String> hobbies;
    private List<String> favoriteFoods;
}
