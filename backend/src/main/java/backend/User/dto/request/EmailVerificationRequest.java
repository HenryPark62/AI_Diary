package backend.User.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailVerificationRequest {
    @NotBlank
    private String email;
}
