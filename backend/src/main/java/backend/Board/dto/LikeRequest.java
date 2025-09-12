package backend.Board.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LikeRequest {
    @NotNull
    private LikeType type;  // POST, COMMENT

    @NotNull
    private Long targetId;  // 게시글 ID 또는 댓글 ID

    public enum LikeType {
        POST, COMMENT
    }
}