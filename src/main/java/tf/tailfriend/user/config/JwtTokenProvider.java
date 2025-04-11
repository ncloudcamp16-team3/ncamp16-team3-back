package tf.tailfriend.user.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKeyString;

    private Key secretKey;

    private final long validityInMilliseconds = 1000 * 60 * 60 * 24; // 1일

    @PostConstruct
    protected void init() {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }


    public String createToken(Integer userId, String email, Integer snsTypeId) {
        Claims claims = Jwts.claims().setSubject(userId.toString());
        claims.put("email", email);
        claims.put("snsTypeId", snsTypeId);

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ 인증 객체 생성 (UserPrincipal에 모든 정보 포함)
    public Authentication getAuthentication(String token) {
        Integer userId = getUserId(token);
        String email = getEmail(token);
        Integer snsTypeId = getSnsTypeId(token);

        UserDetails userDetails = new UserPrincipal(userId, email, snsTypeId);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    // ✅ 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ✅ 유저 정보 추출
    public Integer getUserId(String token) {
        return Integer.parseInt(parseClaims(token).getSubject());
    }

    public String getEmail(String token) {
        return (String) parseClaims(token).get("email");
    }

    public Integer getSnsTypeId(String token) {
        Object snsTypeId = parseClaims(token).get("snsTypeId");
        return snsTypeId instanceof Integer ? (Integer) snsTypeId : Integer.parseInt(snsTypeId.toString());
    }

    // ✅ 클레임 파싱
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
