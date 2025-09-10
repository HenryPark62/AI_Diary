package backend.Board.dto;

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
public class PostPageResponse {
    private List<PostListResponse> posts;    // 게시글 목록
    private int currentPage;                 // 현재 페이지
    private int totalPages;                  // 전체 페이지 수
    private long totalElements;              // 전체 게시글 수
    private boolean hasNext;                 // 다음 페이지 존재 여부
    private boolean hasPrevious;             // 이전 페이지 존재 여부
    private PostSortType sortType;           // 현재 정렬 타입
}