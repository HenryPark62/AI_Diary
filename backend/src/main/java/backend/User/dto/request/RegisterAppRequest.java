package backend.User.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterAppRequest {
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, max = 12)
    private String password;

    @NotBlank
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[\\W_])[A-Za-z0-9\\W_]{8,12}$\n", message = "비밀번호는 8 ~ 12자 이내여야 합니다.")
    private String nickname;
}
