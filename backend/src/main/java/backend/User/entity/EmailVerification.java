package backend.User.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "email_verification")
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "temporary_user_id", referencedColumnName = "temporary_user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private TemporaryUser temporaryUserId;

    // 비밀번호 찾기 시 User와 연관 (OneToMany 관계)
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(nullable = false)
    private int verificationCode;

    @Column(nullable = false, length = 10)
    private boolean isEmailVerified;

    @Column(nullable = false)
    private LocalDateTime createTime;

    @Column(nullable = false)
    private LocalDateTime expirationTime;
}

