package backend.Board.service;

import backend.Board.dto.PostListResponse;
import backend.Board.dto.PostPageResponse;
import backend.Board.dto.PostRequest;
import backend.Board.dto.PostResponse;
import backend.Board.dto.PostSearchRequest;
import backend.Board.dto.PostSortType;
import backend.Board.dto.PostUpdateRequest;
import backend.Board.entity.Disclosure;
import backend.Board.entity.Like;
import backend.Board.entity.Post;
import backend.Board.entity.PostImage;
import backend.Board.repository.LikeRepository;
import backend.Board.repository.PostRepository;
import backend.auth.entity.User;
import backend.auth.service.UserService;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final UserService userService;
    private final PostImageService postImageService;

    private Page<Post> getPostsBySortType(PostSortType sortType, Pageable pageable) {
        return switch (sortType) {
            case LIKES -> postRepository.findByDisclosureOrderByLikesDesc(Disclosure.PUBLIC, pageable);
            case VIEWS -> postRepository.findByDisclosureOrderByViewCountDesc(Disclosure.PUBLIC, pageable);
            case POPULAR ->
                    postRepository.findByDisclosureOrderByCreatedAtDesc(Disclosure.PUBLIC, pageable); // 레딧 스코어는 후처리
            case DATE -> postRepository.findByDisclosureOrderByCreatedAtDesc(Disclosure.PUBLIC, pageable);
        };
    }

    // 게시글 상세 조회
    @Transactional
    public PostResponse getPost(Long postId, Long currentUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 공개 게시글이거나 본인 게시글인 경우만 조회 가능
        if (post.getDisclosure() == Disclosure.PRIVATE && !post.getUser().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("접근 권한이 없습니다.");
        }

        // 공개 게시글이고 본인이 아닌 경우 조회수 증가
        if (post.getDisclosure() == Disclosure.PUBLIC && !post.getUser().getId().equals(currentUserId)) {
            post.setViewCount(post.getViewCount() + 1);
            postRepository.save(post);
        }

        return PostResponse.from(post);
    }

    // 게시글 작성
    @Transactional
    public PostResponse createPost(PostRequest request, List<MultipartFile> images, Long userId) {
        User user = userService.getUserById(userId);

        // 이미지 업로드
        List<String> imageUrls = List.of();
        if (images != null && !images.isEmpty()) {
            imageUrls = postImageService.uploadPostImages(images);
        }

        Post post = Post.builder()
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .emotion(request.getEmotion())
                .disclosure(request.getDisclosure())
                .viewCount(0L)
                .build();

        // PostImage 엔티티 생성 및 추가
        if (!imageUrls.isEmpty()) {
            List<PostImage> postImages = imageUrls.stream()
                    .map(imageUrl -> PostImage.builder()
                            .post(post)
                            .imageUrl(imageUrl)
                            .build())
                    .collect(Collectors.toList());
            post.getImages().addAll(postImages);
        }

        try {
            Post savedPost = postRepository.save(post);
            return PostResponse.from(savedPost);
        } catch (Exception e) {
            // 게시글 저장 실패 시 업로드된 이미지 롤백
            if (!imageUrls.isEmpty()) {
                postImageService.deleteImages(imageUrls);
            }
            throw new RuntimeException("게시글 저장 중 오류가 발생했습니다.", e);
        }
    }

    // 게시글 수정
    @Transactional
    public PostResponse updatePost(Long postId, PostUpdateRequest request, List<MultipartFile> newImages, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 작성자 확인
        if (!post.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        // 기존 이미지 URL 백업 (롤백용)
        List<String> oldImageUrls = post.getImages().stream()
                .map(PostImage::getImageUrl)
                .collect(Collectors.toList());

        // 새 이미지 업로드
        List<String> newImageUrls = List.of();
        if (newImages != null && !newImages.isEmpty()) {
            newImageUrls = postImageService.uploadPostImages(newImages);
        }

        try {
            // 게시글 정보 업데이트
            post.setTitle(request.getTitle());
            post.setContent(request.getContent());
            post.setEmotion(request.getEmotion());
            post.setDisclosure(request.getDisclosure());

            // 기존 이미지 삭제 후 새 이미지 추가
            post.getImages().clear();
            if (!newImageUrls.isEmpty()) {
                List<PostImage> postImages = newImageUrls.stream()
                        .map(imageUrl -> PostImage.builder()
                                .post(post)
                                .imageUrl(imageUrl)
                                .build())
                        .collect(Collectors.toList());
                post.getImages().addAll(postImages);
            }

            Post savedPost = postRepository.save(post);

            // 성공 시 기존 이미지 S3에서 삭제
            if (!oldImageUrls.isEmpty()) {
                postImageService.deleteImages(oldImageUrls);
            }

            return PostResponse.from(savedPost);
        } catch (Exception e) {
            // 실패 시 새로 업로드된 이미지 롤백
            if (!newImageUrls.isEmpty()) {
                postImageService.deleteImages(newImageUrls);
            }
            throw new RuntimeException("게시글 수정 중 오류가 발생했습니다.", e);
        }
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 작성자 확인
        if (!post.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        // 게시글의 이미지 URL 수집
        List<String> imageUrls = post.getImages().stream()
                .map(PostImage::getImageUrl)
                .collect(Collectors.toList());

        // 게시글 삭제
        postRepository.delete(post);

        // S3에서 이미지 삭제
        if (!imageUrls.isEmpty()) {
            postImageService.deleteImages(imageUrls);
        }
    }

    // 게시글 좋아요/좋아요 취소
    @Transactional
    public boolean toggleLike(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 비공개 게시글은 좋아요 불가
        if (post.getDisclosure() == Disclosure.PRIVATE) {
            throw new IllegalArgumentException("비공개 게시글에는 좋아요를 할 수 없습니다.");
        }

        User user = userService.getUserById(userId);

        // 기존 좋아요 확인
        boolean existingLike = likeRepository.existsByPostAndUser(post, user);

        if (existingLike) {
            // 좋아요 취소
            likeRepository.deleteByPostAndUser(post, user);
            return false;
        } else {
            // 좋아요 추가
            Like like = Like.builder()
                    .post(post)
                    .user(user)
                    .build();
            likeRepository.save(like);
            return true;
        }
    }

    // ============ 인기글 고정 기능 ============

    /**
     * 레딧 알고리즘 기반 인기 게시글 4개 조회 (상단 고정용)
     */
    public List<PostListResponse> getPopularPosts() {
        // 최근 게시글을 더 많이 가져와서 레딧 스코어로 정렬
        Pageable pageable = PageRequest.of(0, 50); // 50개 가져와서 상위 4개 선택
        Page<Post> recentPosts = postRepository.findByDisclosureOrderByCreatedAtDesc(Disclosure.PUBLIC, pageable);

        return recentPosts.getContent().stream()
                .map(PostListResponse::from)
                .sorted(Comparator.comparing(PostListResponse::getRedditScore).reversed())
                .limit(4) // 상위 4개만 반환
                .collect(Collectors.toList());
    }

    // ============ 검색 기능 ============

    /**
     * 검색 조건이 있는지 확인
     */
    private boolean hasSearchCondition(PostSearchRequest request) {
        return (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) ||
                (request.getTitle() != null && !request.getTitle().trim().isEmpty()) ||
                (request.getContent() != null && !request.getContent().trim().isEmpty()) ||
                (request.getNickname() != null && !request.getNickname().trim().isEmpty());
    }

    /**
     * 검색 조건에 따른 게시글 조회
     */
    private Page<Post> searchPosts(PostSearchRequest request, Pageable pageable) {
        String keyword = request.getKeyword();
        String title = request.getTitle();
        String content = request.getContent();
        String nickname = request.getNickname();

        // 1. 통합 검색 (keyword가 있는 경우) - 최우선
        if (keyword != null && !keyword.trim().isEmpty()) {
            return postRepository.findByDisclosureAndKeyword(Disclosure.PUBLIC, keyword.trim(), pageable);
        }

        // 2. 복합 검색 조건들
        if (isNotEmpty(title) && isNotEmpty(content)) {
            return postRepository.findByDisclosureAndTitleAndContent(
                    Disclosure.PUBLIC, title.trim(), content.trim(), pageable);
        }

        if (isNotEmpty(title) && isNotEmpty(nickname)) {
            return postRepository.findByDisclosureAndTitleAndNickname(
                    Disclosure.PUBLIC, title.trim(), nickname.trim(), pageable);
        }

        // 3. 단일 검색 조건들
        if (isNotEmpty(title)) {
            return postRepository.findByDisclosureAndTitle(Disclosure.PUBLIC, title.trim(), pageable);
        }

        if (isNotEmpty(content)) {
            return postRepository.findByDisclosureAndContent(Disclosure.PUBLIC, content.trim(), pageable);
        }

        if (isNotEmpty(nickname)) {
            return postRepository.findByDisclosureAndNickname(Disclosure.PUBLIC, nickname.trim(), pageable);
        }

        // 검색 조건이 없으면 기본 조회
        return getPostsBySortType(PostSortType.DATE, pageable);
    }

    /**
     * 문자열이 비어있지 않은지 확인
     */
    private boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * 공개 게시글 목록 조회 (검색 기능 포함)
     */
    public PostPageResponse getPublicPosts(PostSearchRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<Post> posts;

        // 특정 날짜 조회가 있는 경우
        if (request.getDate() != null) {
            posts = postRepository.findByDisclosureAndDate(Disclosure.PUBLIC, request.getDate(), pageable);
        }
        // 감정 필터가 있는 경우
        else if (request.getEmotion() != null) {
            posts = postRepository.findByDisclosureAndEmotionOrderByCreatedAtDesc(
                    Disclosure.PUBLIC, request.getEmotion(), pageable);
        }
        // 검색 조건이 있는 경우 -> 새로 추가된 부분
        else if (hasSearchCondition(request)) {
            posts = searchPosts(request, pageable);
        }
        // 정렬 타입에 따른 조회
        else {
            posts = getPostsBySortType(request.getSortType(), pageable);
        }

        // 인기순의 경우 레딧 알고리즘 적용
        List<PostListResponse> postResponses;
        if (request.getSortType() == PostSortType.POPULAR) {
            postResponses = posts.getContent().stream()
                    .map(PostListResponse::from)
                    .sorted(Comparator.comparing(PostListResponse::getRedditScore).reversed())
                    .collect(Collectors.toList());
        } else {
            postResponses = posts.getContent().stream()
                    .map(PostListResponse::from)
                    .collect(Collectors.toList());
        }

        return PostPageResponse.builder()
                .posts(postResponses)
                .currentPage(posts.getNumber())
                .totalPages(posts.getTotalPages())
                .totalElements(posts.getTotalElements())
                .hasNext(posts.hasNext())
                .hasPrevious(posts.hasPrevious())
                .sortType(request.getSortType())
                .build();
    }

    /**
     * 개인 일기장 조회 (검색 기능 포함)
     */
    public PostPageResponse getMyPosts(Long userId, PostSearchRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<Post> posts;

        if (request.getDate() != null) {
            posts = postRepository.findByUserIdAndDate(userId, request.getDate(), pageable);
        } else if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            // 개인 일기장에서도 키워드 검색 가능
            posts = postRepository.findByUserIdAndKeyword(userId, request.getKeyword().trim(), pageable);
        } else {
            posts = postRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }

        List<PostListResponse> postResponses = posts.getContent().stream()
                .map(PostListResponse::from)
                .collect(Collectors.toList());

        return PostPageResponse.builder()
                .posts(postResponses)
                .currentPage(posts.getNumber())
                .totalPages(posts.getTotalPages())
                .totalElements(posts.getTotalElements())
                .hasNext(posts.hasNext())
                .hasPrevious(posts.hasPrevious())
                .sortType(PostSortType.DATE)
                .build();
    }
}