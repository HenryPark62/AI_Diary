package backend.Board.repository;

import backend.Board.entity.Comment;
import backend.Board.entity.Post;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 게시글의 최상위 댓글 조회
    List<Comment> findByPostAndParentIsNullOrderByCreatedAtAsc(Post post);

    // 대댓글 조회
    List<Comment> findByParentOrderByCreatedAtAsc(Comment parent);

    // 게시글의 모든 댓글 수 조회
    long countByPost(Post post);
}