package backend.User.service;

import backend.User.config.exception.BadRequestException;
import backend.User.config.exception.CustomS3Exception;
import backend.User.dto.request.DeleteRequest;
import backend.User.dto.request.RegisterFinalRequest;
import backend.User.dto.response.LoginResponse;
import backend.User.entity.DeleteReason;
import backend.User.entity.Gender;
import backend.User.entity.MBTI;
import backend.User.entity.TemporaryUser;
import backend.User.entity.User;
import backend.User.repository.EmailVerificationRepository;
import backend.User.repository.TemporaryUserRepository;
import backend.User.repository.UserRepository;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
//import com.amazonaws.util.Platform;
//import com.trip.planit.User.config.exception.BadRequestException;
//import com.trip.planit.User.config.exception.CustomS3Exception;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    private final AmazonS3 amazonS3;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TemporaryUserRepository temporaryUserRepository;
    private final EmailVerificationRepository emailVerificationRepository;

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Invalid email: " + email));
    }

    public User getUserByEmailOrNull(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public LoginResponse loginResponse(User user) {
        return LoginResponse.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .mbti(user.getMbti())
                .gender(user.getGender())
                .hobbies(stringToList(user.getHobbies()))
                .favoriteFoods(stringToList(user.getFavoriteFoods()))
                .build();
    }

    private List<String> stringToList(String value) {
        return (value == null || value.isBlank()) ?
                Collections.emptyList() :
                Arrays.stream(value.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();
    }

    private String listToString(List<String> list) {
        return (list == null || list.isEmpty()) ? "" : String.join(",", list);
    }

    public String uploadProfileImage(MultipartFile profileImage) {
        String originalFilename = profileImage.getOriginalFilename();
        String savedFilename = UUID.randomUUID().toString() + "_" + originalFilename;
        String key = "profile-images/" + savedFilename;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(profileImage.getSize());
        metadata.setContentType(profileImage.getContentType());

        try {
            amazonS3.putObject(bucketName, key, profileImage.getInputStream(), metadata);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload profile image to S3", e);
        }

        return amazonS3.getUrl(bucketName, key).toString();
    }

    public void deleteFile(String fileUrl) {
        String key = extractFileName(fileUrl);

        try {
            amazonS3.deleteObject(new DeleteObjectRequest(bucketName, key));
        } catch (AmazonServiceException e) {
            throw new CustomS3Exception("AmazonServiceException: " + e.getErrorMessage(), e);
        } catch (SdkClientException e) {
            throw new CustomS3Exception("SdkClientException: " + e.getMessage(), e);
        }
    }

    private String extractFileName(String fileUrl) {
        String httpsPrefix = "https://버킷 주소~/";
        String s3Prefix = "s3://버킷명~/";

        if (fileUrl.startsWith(httpsPrefix)) {
            return fileUrl.replace(httpsPrefix, "");
        } else if (fileUrl.startsWith(s3Prefix)) {
            return fileUrl.replace(s3Prefix, "");
        }

        return fileUrl;
    }

    public String getProfileImageUrl(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        return user.getProfile();
    }

    public void updateUserProfileImage(Long userId, String newProfileUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));
        user.setProfile(newProfileUrl);
        userRepository.save(user);
    }

    public void saveTemporaryUser(String email, String password, String nickname) {
        if (email == null || email.isBlank()) {
            throw new BadRequestException("이메일은 필수입니다.");
        }
        if (password == null || password.isBlank()) {
            throw new BadRequestException("비밀번호는 필수입니다.");
        }
        if (nickname == null || nickname.isBlank()) {
            throw new BadRequestException("닉네임은 필수입니다.");
        }

        TemporaryUser temporaryUser = TemporaryUser.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .nickname(nickname)
                .createdAt(LocalDateTime.now())
                .failedAttempts(0)
                .build();

        temporaryUserRepository.save(temporaryUser);
    }

    @Transactional
    public void completeFinalRegistration(RegisterFinalRequest request, String profile) {
        TemporaryUser tempUser = temporaryUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("임시 사용자를 찾을 수 없습니다."));

        if (userRepository.existsByNickname(tempUser.getNickname())) {
            throw new BadRequestException("이미 사용 중인 닉네임입니다!");
        }

        emailVerificationRepository.deleteByTemporaryUserId_Email(request.getEmail());

        completeRegistration(tempUser, request.getMbti(), request.getGender(), profile,
                request.getHobbies(), request.getFavoriteFoods());
    }

    @Transactional
    public void completeRegistration(TemporaryUser tempUser, MBTI mbti, Gender gender,
                                     String profile, List<String> hobbies, List<String> favoriteFoods) {

        User user = User.builder()
                .email(tempUser.getEmail())
                .password(tempUser.getPassword())
                .nickname(tempUser.getNickname())
                .mbti(mbti)
                .gender(gender)
                .profile(profile)
                .hobbies(listToString(hobbies))
                .favoriteFoods(listToString(favoriteFoods))
                .createdAt(LocalDateTime.now())
                .active(true)
                .failedAttempts(0)
                .build();

        userRepository.save(user);
        temporaryUserRepository.delete(tempUser);
    }

    @Transactional
    public void deactivate(Long userId, DeleteRequest deleteRequest) {
        if (deleteRequest.getDeleteReason() == DeleteReason.OTHER &&
                (deleteRequest.getDeleteReason_Description() == null || deleteRequest.getDeleteReason_Description().isEmpty())) {
            throw new BadRequestException("Please provide a detailed reason when selecting 'Other' as the withdrawal reason.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));
        user.setDeleteReason(deleteRequest.getDeleteReason());
        user.setDeleteReason_Description(deleteRequest.getDeleteReason_Description());
        user.setActive(false);
        user.setDeletionScheduledAt(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);
    }

    @Scheduled(cron = "0 00 9 * * *")
    @Transactional
    public void deleteUsers() {
        LocalDateTime now = LocalDateTime.now();
        List<User> usersToDelete = userRepository.findByActiveFalseAndDeletionScheduledAtBefore(now);
        if (!usersToDelete.isEmpty()) {
            for (User user : usersToDelete) {
                String profileImageUrl = user.getProfile();
                if (profileImageUrl != null && !profileImageUrl.trim().isEmpty()) {
                    deleteFile(profileImageUrl);
                }
            }
            userRepository.deleteAll(usersToDelete);
        }
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            String email = ((UserDetails) authentication.getPrincipal()).getUsername();
            return getUserByEmail(email).getUserId();
        }
        throw new BadRequestException("User is not authenticated.");
    }

    @Transactional
    public void updateFcmToken(Long userId, String fcmToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));
        user.setFcmToken(fcmToken);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + userId));
    }
}
