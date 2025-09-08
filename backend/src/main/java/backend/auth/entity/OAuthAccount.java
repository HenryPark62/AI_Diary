package backend.auth.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "oauth_account",
        uniqueConstraints = @UniqueConstraint(name="uq_oauth_provider_user", columnNames = {"provider", "provider_user_id"}))
@Getter
@Setter
@NoArgsConstructor
public class OAuthAccount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="user_id", nullable=false, foreignKey=@ForeignKey(name="fk_oauth_user"))
    private User user;

    @Column(nullable=false, length=30)
    private String provider; // "google" | "kakao"

    @Column(name="provider_user_id", nullable=false, length=100)
    private String providerUserId;

    @Column(name="email_at_link_time", length=200)
    private String emailAtLinkTime;
}

