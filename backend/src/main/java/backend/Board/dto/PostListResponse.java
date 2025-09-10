package backend.Board.dto;

import backend.Board.entity.Disclosure;
import backend.Board.entity.Emotion;
import backend.Board.entity.Post;
import java.time.LocalDateTime;
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
public class PostListResponse {
    private Long id;
    private String title;
    private String nickname;
    private Emotion emotion;
    private Disclosure disclosure;
    private Long likeCount;
    private Long commentCount;
    private Long viewCount;
    private LocalDateTime createdAt;
    private String thumbnailImage;           // 첫 번째 이미지
    private Double redditScore;              // 레딧 알고리즘 스코어 (인기순용)

    public static PostListResponse from(Post post) {
        double redditScore = calculateRedditScore(post);

        return PostListResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .nickname(post.getUser().getNickName())
                .emotion(post.getEmotion())
                .disclosure(post.getDisclosure())
                .likeCount(post.getDisclosure() == Disclosure.PUBLIC ?
                        (long) post.getLikes().size() : null)
                .commentCount(post.getDisclosure() == Disclosure.PUBLIC ?
                        (long) post.getComments().size() : null)
                .viewCount(post.getDisclosure() == Disclosure.PUBLIC ?
                        post.getViewCount() : null)
                .createdAt(post.getCreatedAt())
                .thumbnailImage(post.getImages().isEmpty() ? null :
                        post.getImages().get(0).getImageUrl())
                .redditScore(redditScore)
                .build();
    }

    // 레딧 알고리즘 스코어 계산
    private static double calculateRedditScore(Post post) {
        if (post.getDisclosure() != Disclosure.PUBLIC) {
            return 0.0;
        }

        long likes = post.getLikes().size();
        long comments = post.getComments().size();
        long views = post.getViewCount();

        // 시간 가중치 (시간이 지날수록 점수 감소)
        long hoursOld = java.time.Duration.between(post.getCreatedAt(), LocalDateTime.now()).toHours();
        double timeDecay = Math.pow(0.8, hoursOld / 24.0); // 24시간마다 20% 감소

        // 레딧 스타일 스코어 계산
        // 좋아요는 높은 가중치, 댓글은 중간, 조회수는 낮은 가중치
        double score = (likes * 10 + comments * 5 + views * 0.1) * timeDecay;

        return Math.max(score, 0.0);
    }
}