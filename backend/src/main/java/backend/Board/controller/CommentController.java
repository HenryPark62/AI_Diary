package backend.Board.controller;

import backend.Board.dto.CommentRequest;
import backend.Board.dto.CommentResponse;
import backend.Board.service.CommentService;
import backend.auth.dto.ApiResponse;
import backend.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "댓글 API", description = "댓글 관련 API")
@RestController
@RequestMapping("/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;

    @Operation(summary = "댓글 작성", description = "댓글을 작성합니다.")
    @PostMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @Parameter(description = "게시글 ID") @PathVariable Long postId,
            @Valid @RequestBody CommentRequest request) {
        try {
            Long userId = getCurrentUserId();
            CommentResponse response = commentService.createComment(postId, request, userId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.onSuccess(response));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("비공개")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.onFailure("FORBIDDEN", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.onFailure("VALIDATION_ERROR", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "댓글 작성 중 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "댓글 수정", description = "댓글을 수정합니다.")
    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @Parameter(description = "댓글 ID") @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest request) {
        try {
            Long userId = getCurrentUserId();
            CommentResponse response = commentService.updateComment(commentId, request, userId);
            return ResponseEntity.ok(ApiResponse.onSuccess(response));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("권한")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.onFailure("FORBIDDEN", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.onFailure("COMMENT_NOT_FOUND", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "댓글 수정 중 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<String>> deleteComment(
            @Parameter(description = "댓글 ID") @PathVariable Long commentId) {
        try {
            Long userId = getCurrentUserId();
            commentService.deleteComment(commentId, userId);
            return ResponseEntity.ok(ApiResponse.onSuccess("댓글이 삭제되었습니다."));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("권한")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.onFailure("FORBIDDEN", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.onFailure("COMMENT_NOT_FOUND", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "댓글 삭제 중 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "게시글 댓글 목록 조회", description = "특정 게시글의 댓글 목록을 조회합니다.")
    @GetMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getCommentsByPost(
            @Parameter(description = "게시글 ID") @PathVariable Long postId) {
        try {
            Long currentUserId = getCurrentUserIdOrNull();
            List<CommentResponse> response = commentService.getCommentsByPost(postId, currentUserId);
            return ResponseEntity.ok(ApiResponse.onSuccess(response));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("권한")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.onFailure("FORBIDDEN", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.onFailure("POST_NOT_FOUND", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "댓글 조회 중 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "댓글 좋아요/취소", description = "댓글에 좋아요를 추가하거나 취소합니다.")
    @PostMapping("/{commentId}/like")
    public ResponseEntity<ApiResponse<String>> toggleCommentLike(
            @Parameter(description = "댓글 ID") @PathVariable Long commentId) {
        try {
            Long userId = getCurrentUserId();
            boolean liked = commentService.toggleLike(commentId, userId);
            String message = liked ? "좋아요가 추가되었습니다." : "좋아요가 취소되었습니다.";
            return ResponseEntity.ok(ApiResponse.onSuccess(message));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("비공개")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.onFailure("FORBIDDEN", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.onFailure("COMMENT_NOT_FOUND", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "좋아요 처리 중 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "대댓글 조회", description = "특정 댓글의 대댓글을 조회합니다.")
    @GetMapping("/{commentId}/replies")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getReplies(
            @Parameter(description = "부모 댓글 ID") @PathVariable Long commentId) {
        try {
            List<CommentResponse> response = commentService.getReplies(commentId);
            return ResponseEntity.ok(ApiResponse.onSuccess(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.onFailure("COMMENT_NOT_FOUND", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "대댓글 조회 중 오류가 발생했습니다."));
        }
    }

    // 현재 로그인한 사용자 ID 조회
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userService.getUserByEmail(email).getId();
    }

    // 현재 로그인한 사용자 ID 조회 (null 허용)
    private Long getCurrentUserIdOrNull() {
        try {
            return getCurrentUserId();
        } catch (Exception e) {
            return null;
        }
    }
}