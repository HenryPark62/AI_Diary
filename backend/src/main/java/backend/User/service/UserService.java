package backend.User.service;

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
import com.amazonaws.util.Platform;
import com.trip.planit.User.config.exception.BadRequestException;
import com.trip.planit.User.config.exception.CustomS3Exception;

import java.util.Arrays;
import java.util.Collections;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

//    private final AmazonS3 amazonS3;
//    @Value("${aws.s3.bucketName}")
//    private String bucketName;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TemporaryUserRepository temporaryUserRepository;
    private final EmailVerificationRepository emailVerificationRepository;

    // 로그인 - User에서 Email 찾기
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email: " + email));
    }

    public User getUserByEmailOrNull(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    // 로그인 - response 값
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

    // 문자열로 저장된 값을 리스트로 변환
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


//    // 이미지 업로드
//    public String uploadProfileImage(MultipartFile profileImage) {
//        String originalFilename = profileImage.getOriginalFilename();
//        String savedFilename = UUID.randomUUID().toString() + "_" + originalFilename;
//        String key = "profile-images/" + savedFilename; // S3 내 저장 경로
//
//        ObjectMetadata metadata = new ObjectMetadata();
//        metadata.setContentLength(profileImage.getSize());
//        metadata.setContentType(profileImage.getContentType());
//
//        try {
//            amazonS3.putObject(bucketName, key, profileImage.getInputStream(), metadata);
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to upload profile image to S3", e);
//        }
//
//        return amazonS3.getUrl(bucketName, key).toString();
//    }
//
//    // S3 삭제 메서드 예시
//    public void deleteFile(String fileUrl) {
//        String key = extractFileName(fileUrl);  // S3 객체 key 추출
//        System.out.println("Deleting S3 object with key: " + key);  // 디버그 로그
//
//        try {
//            amazonS3.deleteObject(new DeleteObjectRequest(bucketName, key));
//        } catch (AmazonServiceException e) {
//            throw new CustomS3Exception("AmazonServiceException: " + e.getErrorMessage(), e);
//        } catch (SdkClientException e) {
//            throw new CustomS3Exception("SdkClientException: " + e.getMessage(), e);
//        }
//    }
//
//    private String extractFileName(String fileUrl) {
//        String httpsPrefix = "https://planitbucket123.s3.amazonaws.com/";
//        String s3Prefix = "s3://planitbucket123/";
//
//        if (fileUrl.startsWith(httpsPrefix)) {
//            return fileUrl.replace(httpsPrefix, "");
//        } else if (fileUrl.startsWith(s3Prefix)) {
//            return fileUrl.replace(s3Prefix, "");
//        }
//
//        // 접두어가 다르면 그대로 반환하거나, 추가 처리를 할 수 있음.
//        return fileUrl;
//    }
//
//    public String getProfileImageUrl(Long userId) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new BadRequestException("User not found"));
//
//        return user.getProfile();
//    }
//
//    // 업데이트
//    public void updateUserProfileImage(Long userId, String newProfileUrl) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new BadRequestException("User not found"));
//        user.setProfile(newProfileUrl);
//        userRepository.save(user);
//    }

    // 회원가입 1단계 - 임시 회원으로 저장(이메일, 비밀번호, 닉네임)
    public void saveTemporaryUser(String email, String password, String nickname) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        }
        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("닉네임은 필수입니다.");
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


    // 회원가입 3단계 - , MBTI, 성별 입력 및 최종 회원가입 자동 완료
    @Transactional
    public void completeFinalRegistration(RegisterFinalRequest request, String profile) {
        TemporaryUser tempUser = temporaryUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("임시 사용자를 찾을 수 없습니다."));

        // 닉네임 중복 체크
        String nickname = tempUser.getNickname();
        if (userRepository.existsByNickname(nickname)) {
            throw new BadRequestException("이미 사용 중인 닉네임입니다!");
        }

        // email_verification 테이블에서 해당 임시 사용자와 관련된 모든 레코드를 먼저 삭제
        emailVerificationRepository.deleteByTemporaryUserId_Email(request.getEmail());

        // 최종 회원가입 처리
        completeRegistration(tempUser, request.getMbti(), request.getGender(), profile,
                request.getHobbies(), request.getFavoriteFoods());
    }

    // 회원가입 - 최종 회원가입 완료
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

        // 임시 사용자 정보 삭제
        temporaryUserRepository.delete(tempUser);
    }

//    @Transactional
//    public void deactivate(Long userId, DeleteReqeust deleteReqeust) {
//        // "기타"를 선택한 경우 상세 사유 검증
//        if (deleteReqeust.getDeleteReason() == DeleteReason.OTHER &&
//                (deleteReqeust.getDeleteReason_Description() == null || deleteReqeust.getDeleteReason_Description()
//                        .isEmpty())) {
//            throw new BadRequestException(
//                    "Please provide a detailed reason when selecting 'Other' as the withdrawal reason.");
//        }
//
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new IllegalArgumentException("User not found"));
//        user.setDeleteReason(deleteReqeust.getDeleteReason());
//        user.setDeleteReason_Description(deleteReqeust.getDeleteReason_Description());
//        user.setActive(false);
//        userRepository.save(user);
//
////        개발 test용 10분 후로 설정.
//        user.setDeletionScheduledAt(LocalDateTime.now().plusMinutes(10));
//
////        개발 test용 4시간 후로 설정.
////        user.setDeletionScheduledAt(LocalDateTime.now().plusHours(4));
//
////        예약 시각 : 현재 시간 + 3일 후
////        user.setDeletionScheduledAt(LocalDateTime.now().plusDays(3));
//    }


//    //  @Scheduled(cron = "0 0 * * * *")
//    // 개발 test용 오전 9시로 설정
//    @Scheduled(cron = "0 00 9 * * *")
//    @Transactional
//    public void deleteUsers() {
//        LocalDateTime now = LocalDateTime.now();
//        List<User> usersToDelete = userRepository.findByActiveFalseAndDeletionScheduledAtBefore(now);
//        if (!usersToDelete.isEmpty()) {
//            for (User user : usersToDelete) {
//                String profileImageUrl = user.getProfile();
//                if (profileImageUrl != null && !profileImageUrl.trim().isEmpty()) {
//                    deleteFile(profileImageUrl);
//                }
//            }
//            userRepository.deleteAll(usersToDelete);
//        }
//    }

    // email로 사용자 찾기
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // 로그인 된 사용자 정보를 가져옴.
    public Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            String email = ((UserDetails) authentication.getPrincipal()).getUsername(); // 현재 로그인한 사용자의 이메일 가져오기
            return getUserByEmail(email).getUserId(); // 이메일로 User 조회 후 user_id 반환
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