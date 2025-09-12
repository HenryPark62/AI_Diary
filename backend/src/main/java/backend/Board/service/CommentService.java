package backend.Board.service;

import backend.Board.dto.CommentRequest;
import backend.Board.dto.CommentResponse;
import backend.Board.entity.Comment;
import backend.Board.entity.Disclosure;
import backend.Board.entity.Like;
import backend.Board.entity.Post;
import backend.Board.repository.CommentRepository;
import backend.Board.repository.LikeRepository;
import backend.Board.repository.PostRepository;
import backend.auth.entity.User;
import backend.auth.service.UserService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final UserService userService;

    // 댓글 작성
    @Transactional
    public CommentResponse createComment(Long postId, CommentRequest request, Long userId) {

        try {
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

            if (post.getDisclosure() == Disclosure.PRIVATE && !post.getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("비공개 게시글에는 댓글을 작성할 수 없습니다.");
            }

            User user = userService.getUserById(userId);

            Comment parent = null;
            if (request.getParentId() != null) {
                parent = commentRepository.findById(request.getParentId())
                        .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다."));

                if (!parent.getPost().getId().equals(postId)) {
                    throw new IllegalArgumentException("잘못된 부모 댓글입니다.");
                }
            }

            String contentValue = request.getComments();

            Comment comment = Comment.builder()
                    .user(user)
                    .post(post)
                    .parent(parent)
                    .comments(contentValue)
                    .build();

            Comment savedComment = commentRepository.save(comment);

            return CommentResponse.from(savedComment);
        } catch (Exception e) {
            throw e;
        }
    }

    // 댓글 수정
    @Transactional
    public CommentResponse updateComment(Long commentId, CommentRequest request, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        // 작성자 확인
        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        comment.setComments(request.getComments());
        Comment savedComment = commentRepository.save(comment);
        return CommentResponse.from(savedComment);
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        // 작성자 확인
        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        commentRepository.delete(comment);
    }

    // 댓글 목록 조회 (게시글별)
    public List<CommentResponse> getCommentsByPost(Long postId, Long currentUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 비공개 게시글은 작성자만 댓글 조회 가능
        if (post.getDisclosure() == Disclosure.PRIVATE &&
                (currentUserId == null || !post.getUser().getId().equals(currentUserId))) {
            throw new IllegalArgumentException("접근 권한이 없습니다.");
        }

        // 최상위 댓글만 조회 (대댓글은 CommentResponse에서 처리)
        List<Comment> topLevelComments = commentRepository.findByPostAndParentIsNullOrderByCreatedAtAsc(post);

        return topLevelComments.stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());
    }

    // 댓글 좋아요/좋아요 취소
    @Transactional
    public boolean toggleLike(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        // 비공개 게시글의 댓글은 좋아요 불가
        if (comment.getPost().getDisclosure() == Disclosure.PRIVATE) {
            throw new IllegalArgumentException("비공개 게시글의 댓글에는 좋아요를 할 수 없습니다.");
        }

        User user = userService.getUserById(userId);

        // 기존 좋아요 확인
        boolean existingLike = likeRepository.existsByCommentAndUser(comment, user);

        if (existingLike) {
            // 좋아요 취소
            likeRepository.deleteByCommentAndUser(comment, user);
            return false;
        } else {
            // 좋아요 추가
            Like like = Like.builder()
                    .comment(comment)
                    .user(user)
                    .build();
            likeRepository.save(like);
            return true;
        }
    }

    // 대댓글 조회
    public List<CommentResponse> getReplies(Long parentCommentId) {
        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다."));

        List<Comment> replies = commentRepository.findByParentOrderByCreatedAtAsc(parentComment);

        return replies.stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());
    }
}