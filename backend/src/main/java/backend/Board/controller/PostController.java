package backend.Board.controller;

import backend.Board.dto.PostListResponse;
import backend.Board.dto.PostPageResponse;
import backend.Board.dto.PostRequest;
import backend.Board.dto.PostResponse;
import backend.Board.dto.PostSearchRequest;
import backend.Board.dto.PostSortType;
import backend.Board.dto.PostUpdateRequest;
import backend.Board.service.PostService;
import backend.auth.dto.ApiResponse;
import backend.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "게시글 API", description = "게시글 관련 API")
@RestController
@RequestMapping("/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final UserService userService;

    @Operation(summary = "공개 게시글 목록 조회", description = "공개된 게시글들을 정렬 옵션과 함께 조회합니다. (검색 기능 포함)")
    @GetMapping("/public")
    public ResponseEntity<ApiResponse<PostPageResponse>> getPublicPosts(
            @ModelAttribute PostSearchRequest request) {
        try {
            PostPageResponse response = postService.getPublicPosts(request);
            return ResponseEntity.ok(ApiResponse.onSuccess(response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "게시글 조회 중 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "내 게시글 목록 조회", description = "현재 사용자의 게시글들을 조회합니다.")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PostPageResponse>> getMyPosts(
            @ModelAttribute PostSearchRequest request) {
        try {
            Long userId = getCurrentUserId();
            PostPageResponse response = postService.getMyPosts(userId, request);
            return ResponseEntity.ok(ApiResponse.onSuccess(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.onFailure("UNAUTHORIZED", "로그인이 필요합니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "게시글 조회 중 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "게시글 상세 조회", description = "특정 게시글의 상세 정보를 조회합니다.")
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> getPost(
            @Parameter(description = "게시글 ID") @PathVariable Long postId) {
        try {
            Long currentUserId = getCurrentUserIdOrNull();
            PostResponse response = postService.getPost(postId, currentUserId);
            return ResponseEntity.ok(ApiResponse.onSuccess(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.onFailure("POST_NOT_FOUND", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "게시글 조회 중 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "게시글 작성", description = "새로운 게시글을 작성합니다. (이미지 0~3장 업로드 가능)")
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @RequestPart("post") @Valid PostRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        try {
            Long userId = getCurrentUserId();
            PostResponse response = postService.createPost(request, images, userId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.onSuccess(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.onFailure("VALIDATION_ERROR", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "게시글 작성 중 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "게시글 수정", description = "기존 게시글을 수정합니다. (이미지 0~3장 업로드 가능)")
    @PutMapping(value = "/{postId}", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @Parameter(description = "게시글 ID") @PathVariable Long postId,
            @RequestPart("post") @Valid PostUpdateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        try {
            Long userId = getCurrentUserId();
            PostResponse response = postService.updatePost(postId, request, images, userId);
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
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "게시글 수정 중 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<String>> deletePost(
            @Parameter(description = "게시글 ID") @PathVariable Long postId) {
        try {
            Long userId = getCurrentUserId();
            postService.deletePost(postId, userId);
            return ResponseEntity.ok(ApiResponse.onSuccess("게시글이 삭제되었습니다."));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("권한")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.onFailure("FORBIDDEN", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.onFailure("POST_NOT_FOUND", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "게시글 삭제 중 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "게시글 좋아요/취소", description = "게시글에 좋아요를 추가하거나 취소합니다.")
    @PostMapping("/{postId}/like")
    public ResponseEntity<ApiResponse<String>> togglePostLike(
            @Parameter(description = "게시글 ID") @PathVariable Long postId) {
        try {
            Long userId = getCurrentUserId();
            boolean liked = postService.toggleLike(postId, userId);
            String message = liked ? "좋아요가 추가되었습니다." : "좋아요가 취소되었습니다.";
            return ResponseEntity.ok(ApiResponse.onSuccess(message));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("비공개")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.onFailure("FORBIDDEN", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.onFailure("POST_NOT_FOUND", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "좋아요 처리 중 오류가 발생했습니다."));
        }
    }

    // ============ 인기글 고정 API ============

    @Operation(summary = "인기 게시글 조회", description = "레딧 알고리즘 기반 인기 게시글 4개를 조회합니다.")
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<PostListResponse>>> getPopularPosts() {
        try {
            List<PostListResponse> response = postService.getPopularPosts();
            return ResponseEntity.ok(ApiResponse.onSuccess(response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "인기 게시글 조회 중 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "게시글 검색", description = "제목, 내용, 닉네임으로 게시글을 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PostPageResponse>> searchPosts(
            @RequestParam(required = false) String keyword,    // 통합 검색
            @RequestParam(required = false) String title,      // 제목 검색
            @RequestParam(required = false) String content,    // 내용 검색
            @RequestParam(required = false) String nickname,   // 닉네임 검색
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "16") int size,
            @RequestParam(defaultValue = "DATE") PostSortType sortType) {
        try {
            PostSearchRequest request = PostSearchRequest.builder()
                    .keyword(keyword)
                    .title(title)
                    .content(content)
                    .nickname(nickname)
                    .page(page)
                    .size(size)
                    .sortType(sortType)
                    .build();

            PostPageResponse response = postService.getPublicPosts(request);
            return ResponseEntity.ok(ApiResponse.onSuccess(response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "게시글 검색 중 오류가 발생했습니다."));
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