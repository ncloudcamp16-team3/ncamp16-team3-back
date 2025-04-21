package tf.tailfriend.global.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtTokenProvider implements InitializingBean {

    @Value("${jwt.secret}")
    private String secretKeyString;

    private Key key;
    private final long tokenValidityInMilliseconds;

    public JwtTokenProvider(@Value("${jwt.token-validity}") long tokenValidityInMilliseconds
    ) {
        this.tokenValidityInMilliseconds = tokenValidityInMilliseconds * 1000;
    }

    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    // 관리자 페이지
    @Override
    public void afterPropertiesSet() throws Exception {
        byte[] keyBytes = secretKeyString.getBytes();
        this.key = Keys.hmacShaKeyFor(keyBytes);
        log.info("JWT token key: {}", this.key);
    }

    public String createToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date validity = new Date(now + tokenValidityInMilliseconds);

        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim("auth", authorities)
                .signWith(key, SignatureAlgorithm.HS256)
                .setExpiration(validity)
                .compact();
    }

    // APP 페이지
    public String createToken(Integer userId, String snsAccountId, Integer snsTypeId, Boolean isNewUser) {
        Claims claims = Jwts.claims().setSubject(userId.toString());
        claims.put("snsAccountId", snsAccountId);
        claims.put("snsTypeId", snsTypeId);
        claims.put("isNewUser", isNewUser);

        Date now = new Date();
        Date validity = new Date(now.getTime() + tokenValidityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        if (claims.get("auth") != null) {
            // 관리자 페이지
            Collection<? extends GrantedAuthority> authorities =
                    Arrays.stream(claims.get("auth").toString().split(","))
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

            log.info("User Authorities: {}", authorities);

            UserDetails principal = new User(claims.getSubject(), "", authorities);
            return new UsernamePasswordAuthenticationToken(principal, token, authorities);
        } else {
            // APP 페이지
            Integer userId = getUserId(token);
            String snsAccountId = getSnsAccountId(token);
            Integer snsTypeId = getSnsTypeId(token);
            Boolean isNewUser = getIsNewUser(token);

            UserDetails userDetails = new UserPrincipal(userId, snsAccountId, snsTypeId, isNewUser);
            return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
        }
    }

    // ✅ 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("⚠️ Token expired");
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("❌ Invalid token: " + e.getMessage());
        }
        return false;
    }

    // ✅ 클레임 파싱
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ✅ 유저 정보 추출
    public Integer getUserId(String token) {
        return Integer.parseInt(parseClaims(token).getSubject());
    }

    public String getSnsAccountId(String token) {
        return (String) parseClaims(token).get("snsAccountId");
    }

    public Integer getSnsTypeId(String token) {
        Object snsTypeId = parseClaims(token).get("snsTypeId");
        return snsTypeId instanceof Integer ? (Integer) snsTypeId : Integer.parseInt(snsTypeId.toString());
    }

    public Boolean getIsNewUser(String token) {
        Object isNew = parseClaims(token).get("isNewUser");
        return isNew instanceof Boolean ? (Boolean) isNew : Boolean.parseBoolean(isNew.toString());
    }
}
