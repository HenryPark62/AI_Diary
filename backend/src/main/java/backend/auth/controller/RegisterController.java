package backend.auth.controller;

import backend.auth.dto.ApiResponse;
import backend.auth.dto.LoginRequest;
import backend.auth.dto.LoginResponse;
import backend.auth.dto.RegisterRequest;
import backend.auth.entity.User;
import backend.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "[구현 완료] 회원가입 관련 API", description = "회원가입 관련 API")
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/v1/users/register")
@RequiredArgsConstructor
public class RegisterController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 닉네임으로 회원가입을 진행합니다.")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<LoginResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response) {

        try {
            // 이메일 중복 확인
            if (userService.existsByEmail(request.getEmail())) {
                return new ResponseEntity<>(
                        ApiResponse.onFailure("EMAIL_EXISTS", "이미 사용 중인 이메일입니다."),
                        HttpStatus.BAD_REQUEST
                );
            }

            // 닉네임 중복 확인
            if (userService.existsByNickName(request.getNickName())) {
                return new ResponseEntity<>(
                        ApiResponse.onFailure("NICKNAME_EXISTS", "이미 사용 중인 닉네임입니다."),
                        HttpStatus.BAD_REQUEST
                );
            }

            // 회원가입 처리
            User newUser = userService.registerUser(
                    request.getEmail(),
                    request.getPassword(),
                    request.getNickName()
            );

            // 로그인 쿠키 설정
            userService.setLoginCookie(response, newUser.getEmail());

            // 로그인 응답 생성
            LoginResponse loginResponse = userService.loginResponse(newUser);

            return new ResponseEntity<>(
                    ApiResponse.onSuccess(loginResponse),
                    HttpStatus.CREATED
            );
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    ApiResponse.onFailure("VALIDATION_ERROR", e.getMessage()),
                    HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    ApiResponse.onFailure("INTERNAL_ERROR", "회원가입 처리 중 오류가 발생했습니다."),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Operation(summary = "일반 로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        try {
            // 사용자 조회
            User user = userService.getUserByEmailOrNull(request.getEmail());

            // 사용자 존재 여부 및 비밀번호 확인
            if (user == null || !userService.matchesPassword(request.getPassword(), user.getPassword())) {
                return new ResponseEntity<>(
                        ApiResponse.onFailure("INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다."),
                        HttpStatus.UNAUTHORIZED
                );
            }

            // 로그인 쿠키 설정
            userService.setLoginCookie(response, user.getEmail());

            // 로그인 응답 생성
            LoginResponse loginResponse = userService.loginResponse(user);

            return ResponseEntity.ok(ApiResponse.onSuccess(loginResponse));

        } catch (Exception e) {
            return new ResponseEntity<>(
                    ApiResponse.onFailure("INTERNAL_ERROR", "로그인 처리 중 오류가 발생했습니다."),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "JWT 쿠키를 삭제하여 로그아웃 처리합니다.")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletResponse response) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
                String email = userDetails.getUsername();
                User user = userService.getUserByEmailOrNull(email);

                if (user != null) {
                    userService.logoutUser(user.getId(), response);
                }
            }

            return ResponseEntity.ok(ApiResponse.onSuccess("로그아웃 성공"));

        } catch (Exception e) {
            return new ResponseEntity<>(
                    ApiResponse.onFailure("INTERNAL_ERROR", "로그아웃 처리 중 오류가 발생했습니다."),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/{userId}")
    @Operation(summary = "사용자 정보 조회 (ID로)", description = "사용자 ID로 사용자 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<LoginResponse>> getUserById(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            LoginResponse userInfo = userService.loginResponse(user);
            return ResponseEntity.ok(ApiResponse.onSuccess(userInfo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.onFailure("USER_NOT_FOUND", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "사용자 정보 조회 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<LoginResponse>> getMyInfo() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.onFailure("UNAUTHORIZED", "로그인이 필요합니다."));
            }

            String email = ((UserDetails) authentication.getPrincipal()).getUsername();
            User user = userService.getUserByEmail(email);
            LoginResponse userInfo = userService.loginResponse(user);

            return ResponseEntity.ok(ApiResponse.onSuccess(userInfo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.onFailure("USER_NOT_FOUND", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "사용자 정보 조회 중 오류가 발생했습니다."));
        }
    }
}