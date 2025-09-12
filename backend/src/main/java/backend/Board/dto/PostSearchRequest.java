package backend.Board.dto;

import backend.Board.entity.Emotion;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostSearchRequest {
    @Builder.Default
    private int page = 0;                    // 페이지 번호 (0부터 시작)

    @Builder.Default
    private int size = 16;                   // 페이지 크기 (4x4)

    @Builder.Default
    private PostSortType sortType = PostSortType.DATE;  // 정렬 타입

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;                  // 특정 날짜 조회 (선택사항)

    private Emotion emotion;                 // 감정 필터 (선택사항)

    private String keyword;                  // 통합 검색 (제목+내용+닉네임)
    private String title;                    // 제목 검색
    private String content;                  // 내용 검색
    private String nickname;                 // 닉네임 검색
}