//package tf.tailfriend.user.config;
//
//import io.jsonwebtoken.*;
//import io.jsonwebtoken.security.Keys;
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Component;
//
//import java.nio.charset.StandardCharsets;
//import java.security.Key;
//import java.util.Date;
//
//@Component
//@RequiredArgsConstructor
//public class JwtTokenProvider {
//
//    @Value("${jwt.secret}")
//    private String secretKeyString;
//
//    private Key secretKey;
//
//    private final long validityInMilliseconds = 1000 * 60 * 60 * 24; // 1일
//
//    @PostConstruct
//    protected void init() {
//        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
//    }
//
//    public String createToken(Integer userId, String snsAccountId, Integer snsTypeId, Boolean isNewUser) {
//        Claims claims = Jwts.claims().setSubject(userId.toString());
//        claims.put("snsAccountId", snsAccountId);
//        claims.put("snsTypeId", snsTypeId);
//        claims.put("isNewUser", isNewUser);
//
//        Date now = new Date();
//        Date validity = new Date(now.getTime() + validityInMilliseconds);
//
//        return Jwts.builder()
//                .setClaims(claims)
//                .setIssuedAt(now)
//                .setExpiration(validity)
//                .signWith(secretKey, SignatureAlgorithm.HS256)
//                .compact();
//    }
//
//    public Authentication getAuthentication(String token) {
//        Integer userId = getUserId(token);
//        String snsAccountId = getSnsAccountId(token);
//        Integer snsTypeId = getSnsTypeId(token);
//        Boolean isNewUser = getIsNewUser(token);
//
//        UserDetails userDetails = new UserPrincipal(userId, snsAccountId, snsTypeId, isNewUser);
//        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
//    }
//
//    // ✅ 토큰 유효성 검사
//    public boolean validateToken(String token) {
//        try {
//            parseClaims(token);
//            return true;
//        } catch (ExpiredJwtException e) {
//            System.out.println("⚠️ Token expired");
//        } catch (JwtException | IllegalArgumentException e) {
//            System.out.println("❌ Invalid token: " + e.getMessage());
//        }
//        return false;
//    }
//
//    // ✅ 유저 정보 추출
//    public Integer getUserId(String token) {
//        return Integer.parseInt(parseClaims(token).getSubject());
//    }
//
//    public String getSnsAccountId(String token) {
//        return (String) parseClaims(token).get("snsAccountId");
//    }
//
//    public Integer getSnsTypeId(String token) {
//        Object snsTypeId = parseClaims(token).get("snsTypeId");
//        return snsTypeId instanceof Integer ? (Integer) snsTypeId : Integer.parseInt(snsTypeId.toString());
//    }
//
//    public Boolean getIsNewUser(String token) {
//        Object isNew = parseClaims(token).get("isNewUser");
//        return isNew instanceof Boolean ? (Boolean) isNew : Boolean.parseBoolean(isNew.toString());
//    }
//
//    // ✅ 클레임 파싱
//    private Claims parseClaims(String token) {
//        return Jwts.parserBuilder()
//                .setSigningKey(secretKey)
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//    }
//}
