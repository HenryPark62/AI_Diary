package backend.Board.controller;

import backend.Board.dto.LikeRequest;
import backend.Board.dto.LikeRequest.LikeType;
import backend.Board.service.CommentService;
import backend.Board.service.PostService;
import backend.auth.dto.ApiResponse;
import backend.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "좋아요 API", description = "통합 좋아요 관련 API")
@RestController
@RequestMapping("/v1/likes")
@RequiredArgsConstructor
public class LikeController {

    private final PostService postService;
    private final CommentService commentService;
    private final UserService userService;

    @Operation(summary = "좋아요/취소", description = "게시글 또는 댓글에 좋아요를 추가하거나 취소합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<String>> toggleLike(@Valid @RequestBody LikeRequest request) {
        try {
            Long userId = getCurrentUserId();
            boolean liked;

            if (request.getType() == LikeType.POST) {
                liked = postService.toggleLike(request.getTargetId(), userId);
            } else if (request.getType() == LikeType.COMMENT) {
                liked = commentService.toggleLike(request.getTargetId(), userId);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.onFailure("INVALID_TYPE", "올바르지 않은 좋아요 타입입니다."));
            }

            String message = liked ? "좋아요가 추가되었습니다." : "좋아요가 취소되었습니다.";
            return ResponseEntity.ok(ApiResponse.onSuccess(message));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("비공개")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.onFailure("FORBIDDEN", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.onFailure("NOT_FOUND", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "좋아요 처리 중 오류가 발생했습니다."));
        }
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userService.getUserByEmail(email).getId();
    }
}