package backend.Board.entity;

import backend.auth.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "post")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    // 유저정보 (닉네임, 프로필)

    @Column(nullable = false)
    private String title;
    // 게시글 제목

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    // 게시글

    @Enumerated(EnumType.STRING)
    private Emotion emotion;
    // 감정

    @Enumerated(EnumType.STRING)
    private Disclosure disclosure;
    // 공개여부

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
//    빈 리스트 반환이 아니여서 NPE 터지는 것 막기위해 사용
    private List<PostImage> images = new ArrayList<>();
    // 이미지 (0 ~ 3장까지)

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Like> likes = new ArrayList<>();
    // 좋아요

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();
    // 댓글 대댓글

    private long viewCount = 0;
    // 조회수

    @CreationTimestamp
    private LocalDateTime createdAt;
    // 생성일

    @UpdateTimestamp
    private LocalDateTime updatedAt;
    // 수정일

}
