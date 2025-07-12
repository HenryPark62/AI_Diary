package backend.User.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user", indexes = {
        @Index(name = "idx_email", columnList = "email")
})

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Column(name = "nickname", length = 20, nullable = false, unique = true)
    private String nickname;

    //  선택사항
    @Column(name = "profile")
    private String profile;

    @Enumerated(EnumType.STRING)
    @Column(name = "mbti", length = 20)
    private MBTI mbti;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "hobbies", columnDefinition = "TEXT")
    private String hobbies;

    @Column(name = "favorite_foods", columnDefinition = "TEXT")
    private String favoriteFoods;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 게정 활성화 여부
    @Column(name = "active", nullable = false)
    private boolean active;

    // 이메일 인증 실패 횟수를 저장하는 필드 (기본값은 0)
    @Column(nullable = false)
    private int failedAttempts;

    private LocalDateTime deletionScheduledAt; // 예약된 삭제 시각 저장.

    // 탈퇴 사유 1~4
    @Column(name = "deletereason")
    @Enumerated(EnumType.STRING)
    private DeleteReason deleteReason;

    // 탈퇴 사유 기타
    @Column(name = "deleteReason_Description")
    private String deleteReason_Description;

    @Column(name = "fcm_token")
    private String fcmToken;
}