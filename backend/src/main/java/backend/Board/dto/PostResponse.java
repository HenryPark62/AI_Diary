package backend.Board.dto;

import backend.Board.entity.Post;
import backend.Board.entity.Disclosure;
import backend.Board.entity.Emotion;
import backend.Board.entity.PostImage;
import java.time.LocalDateTime;
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
public class PostResponse {
    private Long id;
    private String nickname;
    private String title;
    //  유저정보
    private String content;
    private Emotion emotion;
    private Disclosure disclosure;
    @Builder.Default
    private List<String> images = new ArrayList<>();
    //  게시글 정보
    private Long likeCount;
    @Builder.Default
    private List<CommentResponse> comments = new ArrayList<>();
    private Long viewCount;
    //  작성자가 아닌 타인으로 부터 발생하는 정보
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PostResponse from(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .nickname(post.getUser().getNickName())
                .title(post.getTitle())
                .content(post.getContent())
                .emotion(post.getEmotion())
                .disclosure(post.getDisclosure())
                .images(post.getImages().stream()
                        .map(PostImage::getImageUrl)
                        .toList())
                // PUBLIC일 때만 소셜 기능 포함
                .likeCount(post.getDisclosure() == Disclosure.PUBLIC ?
                        (long) post.getLikes().size() : null)
                .comments(post.getDisclosure() == Disclosure.PUBLIC ?
                        post.getComments().stream().map(CommentResponse::from).toList() :
                        new ArrayList<>())
                .viewCount(post.getDisclosure() == Disclosure.PUBLIC ?
                        post.getViewCount() : null)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
