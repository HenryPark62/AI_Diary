package backend.Board.repository;

import backend.Board.entity.Disclosure;
import backend.Board.entity.Emotion;
import backend.Board.entity.Post;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // 공개 게시글만 조회 (기본)
    Page<Post> findByDisclosureOrderByCreatedAtDesc(Disclosure disclosure, Pageable pageable);

    // 좋아요순 정렬
    @Query("SELECT p FROM Post p LEFT JOIN p.likes l WHERE p.disclosure = :disclosure " +
            "GROUP BY p ORDER BY COUNT(l) DESC, p.createdAt DESC")
    Page<Post> findByDisclosureOrderByLikesDesc(@Param("disclosure") Disclosure disclosure, Pageable pageable);

    // 조회수순 정렬
    Page<Post> findByDisclosureOrderByViewCountDesc(Disclosure disclosure, Pageable pageable);

    // 댓글수순 정렬
    @Query("SELECT p FROM Post p LEFT JOIN p.comments c WHERE p.disclosure = :disclosure " +
            "GROUP BY p ORDER BY COUNT(c) DESC, p.createdAt DESC")
    Page<Post> findByDisclosureOrderByCommentsDesc(@Param("disclosure") Disclosure disclosure, Pageable pageable);

    // 특정 날짜 게시글 조회
    @Query("SELECT p FROM Post p WHERE p.disclosure = :disclosure AND DATE(p.createdAt) = :date " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findByDisclosureAndDate(@Param("disclosure") Disclosure disclosure,
                                       @Param("date") LocalDate date,
                                       Pageable pageable);

    // 감정별 조회
    Page<Post> findByDisclosureAndEmotionOrderByCreatedAtDesc(Disclosure disclosure, Emotion emotion,
                                                              Pageable pageable);

    // 사용자별 게시글 조회 (개인 일기장용)
    Page<Post> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 사용자별 특정 날짜 게시글 조회
    @Query("SELECT p FROM Post p WHERE p.user.id = :userId AND DATE(p.createdAt) = :date " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findByUserIdAndDate(@Param("userId") Long userId,
                                   @Param("date") LocalDate date,
                                   Pageable pageable);

    // 통합 키워드 검색 (제목, 내용, 닉네임)
    @Query("SELECT p FROM Post p WHERE p.disclosure = :disclosure AND " +
            "(p.title LIKE %:keyword% OR p.content LIKE %:keyword% OR p.user.nickName LIKE %:keyword%) " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findByDisclosureAndKeyword(@Param("disclosure") Disclosure disclosure,
                                          @Param("keyword") String keyword,
                                          Pageable pageable);

    // 제목 검색
    @Query("SELECT p FROM Post p WHERE p.disclosure = :disclosure AND p.title LIKE %:title% " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findByDisclosureAndTitle(@Param("disclosure") Disclosure disclosure,
                                        @Param("title") String title,
                                        Pageable pageable);

    // 내용 검색
    @Query("SELECT p FROM Post p WHERE p.disclosure = :disclosure AND p.content LIKE %:content% " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findByDisclosureAndContent(@Param("disclosure") Disclosure disclosure,
                                          @Param("content") String content,
                                          Pageable pageable);

    // 닉네임 검색
    @Query("SELECT p FROM Post p WHERE p.disclosure = :disclosure AND p.user.nickName LIKE %:nickname% " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findByDisclosureAndNickname(@Param("disclosure") Disclosure disclosure,
                                           @Param("nickname") String nickname,
                                           Pageable pageable);

    // 복합 검색 (제목 + 내용)
    @Query("SELECT p FROM Post p WHERE p.disclosure = :disclosure AND " +
            "(p.title LIKE %:title% AND p.content LIKE %:content%) " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findByDisclosureAndTitleAndContent(@Param("disclosure") Disclosure disclosure,
                                                  @Param("title") String title,
                                                  @Param("content") String content,
                                                  Pageable pageable);

    // 복합 검색 (제목 + 닉네임)
    @Query("SELECT p FROM Post p WHERE p.disclosure = :disclosure AND " +
            "(p.title LIKE %:title% AND p.user.nickName LIKE %:nickname%) " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findByDisclosureAndTitleAndNickname(@Param("disclosure") Disclosure disclosure,
                                                   @Param("title") String title,
                                                   @Param("nickname") String nickname,
                                                   Pageable pageable);

    // 사용자별 검색 (개인 일기장용)
    @Query("SELECT p FROM Post p WHERE p.user.id = :userId AND " +
            "(p.title LIKE %:keyword% OR p.content LIKE %:keyword%) " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findByUserIdAndKeyword(@Param("userId") Long userId,
                                      @Param("keyword") String keyword,
                                      Pageable pageable);
}
