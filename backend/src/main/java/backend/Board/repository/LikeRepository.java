package backend.Board.repository;

import backend.Board.entity.Comment;
import backend.Board.entity.Like;
import backend.Board.entity.Post;
import backend.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    // 게시글 좋아요 관련
    boolean existsByPostAndUser(Post post, User user);

    void deleteByPostAndUser(Post post, User user);

    // 댓글 좋아요 관련
    boolean existsByCommentAndUser(Comment comment, User user);

    void deleteByCommentAndUser(Comment comment, User user);

    // 사용자별 좋아요 조회
    boolean existsByPostIdAndUserId(Long postId, Long userId);

    boolean existsByCommentIdAndUserId(Long commentId, Long userId);
}