package backend.User.controller;


import backend.User.entity.User;
import backend.User.repository.UserRepository;
import backend.User.security.CookieUtil;
import backend.User.security.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/oauth2")
@RequiredArgsConstructor
public class OAuth2SuccessController {

    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final UserRepository userRepository;

    @GetMapping("/success")
    public ResponseEntity<?> oauth2Success(@AuthenticationPrincipal OAuth2User principal,
                                           HttpServletResponse response) {
        String email = principal.getAttribute("email");
        User user = userRepository.findByEmail(email).orElseThrow();

        String token = jwtUtil.generateToken(user.getEmail());
        cookieUtil.addJwtCookie(response, "Authorization", token, true);

        return ResponseEntity.ok("Login success");
    }
}

