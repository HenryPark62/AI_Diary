package backend.User.service;

import backend.User.entity.EmailVerification;
import backend.User.entity.TemporaryUser;
import backend.User.entity.User;
import backend.User.repository.EmailVerificationRepository;
import backend.User.repository.TemporaryUserRepository;
import backend.User.repository.UserRepository;
import com.trip.planit.User.config.exception.BadRequestException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final int MAX_ATTEMPTS = 5;
    private static final int VERIFICATION_CODE_EXPIRATION_MINUTES = 5;

    private final JavaMailSender javaMailSender;
    private final EmailVerificationRepository emailVerificationRepository;
    private final TemporaryUserRepository temporaryUserRepository;
    private final UserRepository userRepository;
    private final TemplateEngine templateEngine;

    @Transactional
    public void sendRegistrationVerificationEmail(String email) {
        TemporaryUser tempUser = findTemporaryUserOrThrow(email);
        validateUserNotExists(email);

        deleteExistingTempUserVerification(email);
        int code = generateVerificationCode();

        emailVerificationRepository.save(EmailVerification.builder()
                .temporaryUserId(tempUser)
                .verificationCode(code)
                .isEmailVerified(false)
                .createTime(LocalDateTime.now())
                .expirationTime(LocalDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRATION_MINUTES))
                .build());

        sendEmail(email, "[AI-Diary] 인증코드", generateEmailContent(String.valueOf(code)));
    }

    @Transactional
    public void sendPasswordResetVerificationEmail(String email) {
        User user = findUserOrThrow(email);
        invalidateOldVerificationCodesForUser(user);

        int code = generateVerificationCode();

        emailVerificationRepository.save(EmailVerification.builder()
                .user(user)
                .verificationCode(code)
                .isEmailVerified(false)
                .createTime(LocalDateTime.now())
                .expirationTime(LocalDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRATION_MINUTES))
                .build());

        sendEmail(email, "[AI-Diary] 비밀번호 찾기 인증코드", generateEmailContent(String.valueOf(code)));
    }

    @Transactional
    public boolean verifyEmailCode(String email, int code) {
        TemporaryUser tempUser = findTemporaryUserOrThrow(email);

        EmailVerification verification = emailVerificationRepository
                .findTopByTemporaryUserIdAndVerifiedEmailFalseOrderByCreateTimeDesc(tempUser)
                .orElseThrow(() -> new BadRequestException("인증 정보를 찾을 수 없습니다."));

        validateCodeNotExpired(verification.getExpirationTime());

        if (verification.getVerificationCode() != code) {
            return false;
        }

        verification.setEmailVerified(true);
        emailVerificationRepository.save(verification);
        return true;
    }

    @Transactional
    public boolean verifyUserEmailCode(String email, int code) {
        EmailVerification verification = emailVerificationRepository.findByUser_Email(email)
                .orElseThrow(() -> new BadRequestException("이메일 검증 정보를 찾을 수 없습니다: " + email));

        validateCodeNotExpired(verification.getExpirationTime());

        if (verification.getVerificationCode() != code) {
            return false;
        }

        emailVerificationRepository.delete(verification);
        return true;
    }

    public boolean existsTemporaryUserByEmail(String email) {
        return temporaryUserRepository.findByEmail(email).isPresent();
    }

    public void checkFailedAttempts(int failedAttempts) {
        if (failedAttempts >= MAX_ATTEMPTS) {
            throw new BadRequestException("최대 시도 횟수를 초과했습니다. 새로운 인증 코드를 요청해주세요.");
        }
    }

    // ===== Private Helper Methods ===== //

    private TemporaryUser findTemporaryUserOrThrow(String email) {
        return temporaryUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("임시 사용자를 찾을 수 없습니다."));
    }

    private User findUserOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    private void validateUserNotExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 가입된 사용자입니다.");
        }
    }

    private void validateCodeNotExpired(LocalDateTime expirationTime) {
        if (expirationTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("인증 코드가 만료되었습니다.");
        }
    }

    private void deleteExistingTempUserVerification(String email) {
        emailVerificationRepository.findByTemporaryUserId_Email(email)
                .ifPresent(existing -> {
                    emailVerificationRepository.delete(existing);
                    log.info("기존 인증 코드 삭제됨: {}", email);
                });
    }

    private void invalidateOldVerificationCodesForUser(User user) {
        emailVerificationRepository.findByUser_Email(user.getEmail())
                .ifPresent(emailVerificationRepository::delete);
    }

    private String generateEmailContent(String verificationCode) {
        Context context = new Context();
        context.setVariable("verificationCode", verificationCode);
        return templateEngine.process("verificationEmail", context);
    }

    private void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            log.info("이메일 전송 완료: {}", to);
        } catch (MessagingException e) {
            log.error("이메일 전송 실패: {}", to, e);
            throw new IllegalStateException("이메일 전송에 실패했습니다.", e);
        }
    }

    public int generateVerificationCode() {
        return 1000 + new Random().nextInt(9000); // 4자리 숫자
    }
}

