package backend.User.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    private static final long EXPIRATION_TIME = 86400000; // 1일

    // 서명키 생성
    private Key getSigningKey() {
        // 수정: Base64 디코딩을 io.jsonwebtoken.io.Decoders 사용으로 변경
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 토큰 생성 (이메일과 역할을 포함)
    public String generateToken(String email) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION_TIME);
        System.out.println("Generating token. Current Time: " + now + ", Expiration Time: " + expiration);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // 이메일 추출
    public String extractEmail(String token) {
        String email = extractAllClaims(token).getSubject();
        System.out.println("Extracted Login ID from token: " + email);
        return email;
    }

    // 토큰에서 모든 클레임 추출 (Bearer 접두어 제거 포함)
    private Claims extractAllClaims(String token) {
        token = token.replace("Bearer ", "");
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 토큰 유효성 확인 (이메일 일치 및 만료 여부)
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractEmail(token);
        boolean isValid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        System.out.println("Validating token for username: " + username + ". Token is valid: " + isValid);
        return isValid;
    }

    // 토큰 만료 여부 확인
    private Boolean isTokenExpired(String token) {
        final Date expiration = extractAllClaims(token).getExpiration();
        boolean isExpired = expiration.before(new Date());
        System.out.println("Token expiration time: " + expiration + ". Is token expired: " + isExpired);
        return isExpired;
    }

    // 토큰 만료 시간 반환
    public Date getExpirationDateFromToken(String token) {
        Date expiration = extractAllClaims(token).getExpiration();
        System.out.println("Extracted expiration date from token: " + expiration);
        return expiration;
    }
}
