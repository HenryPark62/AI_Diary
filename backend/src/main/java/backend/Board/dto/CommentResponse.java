package backend.Board.dto;

import backend.Board.entity.Comment;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
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
public class CommentResponse {

    private Long id;
    private String comments;         // 댓글 내용
    private Long writerId;          // 작성자 ID (필요시)
    private String writerNickname;  // 작성자 닉네임
    private Long likeCount;          // 댓글 좋아요 개수
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CommentResponse> replies; // 대댓글

    public static CommentResponse from(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .comments(comment.getComments())
                .writerId(comment.getUser().getId())
                .writerNickname(comment.getUser().getNickName())
                .likeCount((long) comment.getLikes().size())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .replies(comment.getReplies().stream()
                        .map(CommentResponse::from)
                        .collect(Collectors.toList()))
                .build();
    }
}

