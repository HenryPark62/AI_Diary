package backend.User.security;

import backend.User.entity.CookieRule;
import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    @Qualifier("customUserDetailsService")
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;

    private static final String[] EXCLUDED_PATHS = {
            "/public/users/", "/swagger-ui/", "/v3/api-docs/"
    };

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (isExcludedPath(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = extractJwtFromCookies(request.getCookies());
        if (jwt == null || jwt.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = extractEmailFromJwt(jwt);
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticateUser(email, jwt, request);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isExcludedPath(String uri) {
        return Arrays.stream(EXCLUDED_PATHS).anyMatch(uri::startsWith);
    }

    private String extractJwtFromCookies(Cookie[] cookies) {
        if (cookies == null) return null;
        return cookieUtil.resolveTokenFromCookie(cookies, CookieRule.ACCESS_TOKEN_NAME);
    }

    private String extractEmailFromJwt(String token) {
        try {
            String email = jwtUtil.extractEmail(token);
            Date expiration = jwtUtil.getExpirationDateFromToken(token);
            logDebug("Extracted email: " + email + ", expires at: " + expiration);
            return email;
        } catch (Exception e) {
            logDebug("JWT parsing error: " + e.getMessage());
            return null;
        }
    }

    private void authenticateUser(String email, String jwt, HttpServletRequest request) {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            if (jwtUtil.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                logDebug("Authentication successful for " + email);
            } else {
                logDebug("Invalid JWT token");
            }
        } catch (Exception e) {
            logDebug("Authentication error: " + e.getMessage());
        }
    }

    private void logDebug(String message) {
        System.out.println("[JwtRequestFilter] " + message);
    }
}
