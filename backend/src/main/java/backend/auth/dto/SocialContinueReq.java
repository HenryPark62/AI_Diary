package backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class SocialContinueReq {
    @NotBlank
    private String state;

    // 구글에서 이메일이 비공개일 수 있어 대체 입력 허용
    @Email
    private String email;

    private String nickName;

    // 필요 시 약관 등 추가 필드 확장 가능
    // private boolean termsAgreed;
}
