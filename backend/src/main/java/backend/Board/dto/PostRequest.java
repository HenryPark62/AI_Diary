package backend.Board.dto;

import backend.Board.entity.Disclosure;
import backend.Board.entity.Emotion;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostRequest {
    @NotNull(message = "제목은 필수입니다.")
    private String title;
    //  제목

    @NotNull(message = "내용은 필수입니다.")
    private String content;
    //  내용

    @NotNull(message = "감정을 선택해주세요.")
    private Emotion emotion;
    //  감정

    @NotNull(message = "공개여부를 선택해주세요.")
    private Disclosure disclosure;
    //  공개 여부

    private List<String> images = new ArrayList<>();
    //  이미지
}
