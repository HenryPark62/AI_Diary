package backend.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "temporary_user")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TemporaryUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // OAuth2 provider info
    @Column(nullable = false, length = 30)
    private String provider; // e.g. "google"

    @Column(nullable = false, length = 100)
    private String providerId; // sub

    @Column(nullable = true, length = 200)
    private String email; // 구글에서 이메일이 비공개일 수도 있으므로 nullable

    @Column(nullable = true, length = 100)
    private String name;

    // 추가정보 입력 단계에서 사용할 식별자(프론트 리다이렉트 state)
    @Column(nullable = false, length = 80, unique = true)
    private String state; // 랜덤 토큰(임시회원 식별)

    // 만료 시각 (예: 30분)
    private LocalDateTime expiresAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }
}
