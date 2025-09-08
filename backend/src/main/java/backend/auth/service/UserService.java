package backend.auth.service;

import backend.auth.dto.LoginResponse;
import backend.auth.entity.OAuthAccount;
import backend.auth.entity.User;
import backend.auth.repository.OAuthAccountRepository;
import backend.auth.repository.UserRepository;
import backend.auth.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;
    private final OAuthAccountRepository oauthAccountRepository;

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));
    }

    public User getUserByEmailOrNull(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public void setLoginCookie(HttpServletResponse response, String email) {
        jwtService.addAccessTokenCookie(response, email);
    }

    public boolean matchesPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Transactional
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!passwordValidator.isValidPassword(newPassword, email)) {
            throw new IllegalArgumentException("비밀번호 형식이 올바르지 않습니다.\n" + passwordValidator.getPasswordRules());
        }

        String encoded = passwordEncoder.encode(newPassword);
        user.setPassword(encoded);
        userRepository.save(user);
    }

    public LoginResponse loginResponse(User user) {
        return LoginResponse.builder()
                .userId(user.getId())
                .nickName(user.getNickName())
                .build();
    }

    @Transactional
    public User registerUser(String email, String password, String nickName) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 닉네임 중복 확인
        if (userRepository.existsByNickName(nickName)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // 비밀번호 유효성 검사
        if (!passwordValidator.isValidPassword(password, email)) {
            throw new IllegalArgumentException("비밀번호 형식이 올바르지 않습니다.\n" + passwordValidator.getPasswordRules());
        }

        String encodedPassword = passwordEncoder.encode(password);

        User user = User.builder()
                .email(email)
                .password(encodedPassword)
                .nickName(nickName)
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public User registerUserOAuth(String email, String nickName,
                                  String provider, String providerUserId) {

        var existingMap = oauthAccountRepository.findByProviderAndProviderUserId(provider, providerUserId);
        if (existingMap.isPresent()) {
            return existingMap.get().getUser(); // 이미 연결됨 → 바로 로그인
        }

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User u = new User();
            u.setEmail(email);
            u.setNickName(nickName);
            return userRepository.save(u);
        });

        OAuthAccount map = new OAuthAccount();
        map.setUser(user);
        map.setProvider(provider);
        map.setProviderUserId(providerUserId);
        map.setEmailAtLinkTime(email);
        oauthAccountRepository.save(map);

        return user;
    }


    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByNickName(String nickName) {
        return userRepository.existsByNickName(nickName);
    }

    public Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            String email = ((UserDetails) authentication.getPrincipal()).getUsername();
            return getUserByEmail(email).getId();
        }
        throw new IllegalArgumentException("User is not authenticated.");
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다.: " + userId));
    }

    @Transactional
    public void logoutUser(Long userId, HttpServletResponse response) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        jwtService.clearAccessTokenCookie(response);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}