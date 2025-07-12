package backend.User.controller;

import backend.User.apiPayload.code.status.ErrorStatus;
import backend.User.apiPayload.exception.ApiResponse;
import backend.User.apiPayload.exception.GeneralException;
import backend.User.config.exception.BadRequestException;
import backend.User.dto.request.DeleteRequest;
import backend.User.security.JwtService;
import backend.User.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "User Controller(유저 API)")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    // 프로필 사진 수정
    @PutMapping(value = "/profile/change", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "프로필 사진 수정", description = "기존 프로필 사진을 새로운 이미지로 교체")
    public ResponseEntity<ApiResponse<String>> updateProfileImage(
            @Parameter(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart("newProfileImage") MultipartFile newProfileImage
    ) {
        Long userId = userService.getAuthenticatedUserId();

        String existingProfileUrl = userService.getProfileImageUrl(userId);

        // 3) 기존 이미지가 있다면 S3에서 삭제
        if (existingProfileUrl != null && !existingProfileUrl.isEmpty()) {
            userService.deleteFile(existingProfileUrl);
        }

        // 4) 새로운 프로필 이미지를 S3에 업로드
        String newProfileUrl = userService.uploadProfileImage(newProfileImage);

        // 5) 사용자 프로필 업데이트
        userService.updateUserProfileImage(userId, newProfileUrl);

        return ResponseEntity.ok(ApiResponse.onSuccess("Profile image updated successfully."));
    }

    // 프로필 삭제
    @DeleteMapping("/profile/delete")
    @Operation(summary = "프로필 사진 삭제", description = "현재 프로필 사진을 삭제")
    public ResponseEntity<ApiResponse<String>> deleteProfileImage() {
        Long userId = userService.getAuthenticatedUserId();

        // 현재 저장된 프로필 이미지 URL 가져오기
        String existingProfileUrl = userService.getProfileImageUrl(userId);

        if (existingProfileUrl == null || existingProfileUrl.isEmpty()) {
            throw new GeneralException(ErrorStatus.BAD_REQUEST_CUSTOM);
        }

        // S3에서 기존 프로필 이미지 삭제
        userService.deleteFile(existingProfileUrl);

        // 사용자 프로필 정보 업데이트 (프로필 이미지를 NULL로 설정)
        userService.updateUserProfileImage(userId, null);

        return ResponseEntity.ok(ApiResponse.onSuccess("Profile image deleted successfully."));
    }


    // 프로필 조회 API
    @GetMapping("/profile/read")
    @Operation(summary = "프로필 조회", description = "현재 로그인한 사용자의 프로필 정보를 반환")
    public String getProfile() {
        Long userId = userService.getAuthenticatedUserId();

        return userService.getProfileImageUrl(userId);
    }

    // 회원탈퇴
    @DeleteMapping("/user/delete")
    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자의 계정을 삭제 - 비활성화")
    public ResponseEntity<ApiResponse<String>> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletResponse response) {
        try {
            Long userId = userService.getAuthenticatedUserId(); // 현재 로그인한 사용자 ID 가져오기
            userService.deactivate(userId, deleteRequest);
            jwtService.clearAccessTokenCookie(response);
            return ResponseEntity.ok(ApiResponse.onSuccess("User deleted successfully."));
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR_CUSTOM);
        }
    }
}
