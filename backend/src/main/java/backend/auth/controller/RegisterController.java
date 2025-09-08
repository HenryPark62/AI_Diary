package backend.auth.controller;

import backend.auth.dto.*;
import backend.auth.entity.TemporaryUser;
import backend.auth.entity.User;
import backend.auth.service.TemporaryUserService;
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
import org.springframework.web.bind.annotation.*;

@Tag(name = "[구현 완료] 회원가입 관련 API", description = "회원가입 관련 API")
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/v1/users/register")
@RequiredArgsConstructor
public class RegisterController {

    private final UserService userService;
    private final TemporaryUserService tempUserService;

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

    @Operation(summary = "소셜 가입 시작(임시유저 조회)", description = "OAuth2 로그인 성공 후 state로 임시가입 정보를 조회합니다.")
    @GetMapping("/social/start")
    public ResponseEntity<ApiResponse<SocialStartRes>> socialStart(@RequestParam(required = false) String state,
                                                                   @org.springframework.web.bind.annotation.CookieValue(name = "signupState", required = false) String stateCookie) {
        try {
            String effective = (state != null && !state.isBlank()) ? state : stateCookie;
            if (effective == null || effective.isBlank()) {
                return ResponseEntity.badRequest().body(ApiResponse.onFailure("STATE_MISSING", "state 값이 없습니다.(query/cookie)"));
            }
            TemporaryUser tu = tempUserService.getByStateOrThrow(effective);
            SocialStartRes res = SocialStartRes.from(tu);
            return ResponseEntity.ok(ApiResponse.onSuccess(res));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.onFailure("STATE_INVALID", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "임시정보 조회 중 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "소셜 가입 완료", description = "state와 추가정보(필요시 이메일/닉네임)를 제출하여 정식 회원을 생성합니다.")
    @PostMapping("/social/continue")
    public ResponseEntity<ApiResponse<LoginResponse>> socialComplete(
            @Valid @RequestBody SocialContinueReq req,
            HttpServletResponse response) {
        try {
            // 1) 임시유저 검증/조회
            TemporaryUser tu = tempUserService.getByStateOrThrow(req.getState());

            // 2) 이메일 확정 (구글이 비공개면 프론트에서 받아온 값을 사용)
            String email = (tu.getEmail() != null && !tu.getEmail().isBlank()) ? tu.getEmail() : req.getEmail();
            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body(ApiResponse.onFailure("EMAIL_REQUIRED", "이메일이 필요합니다."));
            }

            // 3) 중복 방지
            if (userService.existsByEmail(email)) {
                // 이미 가입된 경우 → 로그인 처리로 전환
                User existing = userService.getUserByEmail(email);
                userService.setLoginCookie(response, existing.getEmail());
                return ResponseEntity.ok(ApiResponse.onSuccess(userService.loginResponse(existing)));
            }

            // 4) 정식 유저 생성 (소셜 메타 포함)
            User newUser = userService.registerUserOAuth(
                    email,
                    (req.getNickName() != null && !req.getNickName().isBlank()) ? req.getNickName()
                            : (tu.getName() != null ? tu.getName() : "User"),
                    tu.getProvider(),                // "google"
                    tu.getProviderId()
            );

            // 5) 임시유저 제거 & 로그인 쿠키 설정
            tempUserService.deleteByState(req.getState());
            userService.setLoginCookie(response, newUser.getEmail());

            // 6) 응답
            return new ResponseEntity<>(ApiResponse.onSuccess(userService.loginResponse(newUser)), HttpStatus.CREATED);

        } catch (IllegalStateException e) {
            return new ResponseEntity<>(ApiResponse.onFailure("STATE_INVALID", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(ApiResponse.onFailure("VALIDATION_ERROR", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(ApiResponse.onFailure("INTERNAL_ERROR", "소셜 가입 처리 중 오류가 발생했습니다."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}