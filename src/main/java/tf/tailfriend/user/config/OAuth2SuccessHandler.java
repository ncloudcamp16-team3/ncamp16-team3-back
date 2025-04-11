package tf.tailfriend.user.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tf.tailfriend.user.service.UserService;
import jakarta.servlet.http.Cookie;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        var attributes = oAuth2User.getAttributes();

        String email = OAuth2AttributeExtractor.getEmail(attributes);
        String snsAccountId = OAuth2AttributeExtractor.getSnsAccountId(attributes);
        Integer snsTypeId = OAuth2AttributeExtractor.getSnsTypeId(attributes);

        // 가입 여부 확인
        Integer userId = userService.getUserIdBySnsAccountId(snsAccountId);
        boolean isNewUser = (userId == null);
        if (isNewUser) {
            userId = -1; // 아직 DB에 없음
        }

        // ✅ JWT 발급 (HttpOnly 쿠키)
        String token = jwtTokenProvider.createToken(userId, email, snsTypeId);
        ResponseCookie loginCookie = ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                .secure(false) // 운영 환경에선 true로 설정
                .path("/")
                .maxAge(Duration.ofHours(1))
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", loginCookie.toString());

        // ✅ 회원가입 필요 시 signupInfo 쿠키도 발급 (JS에서 읽을 수 있음)
        if (isNewUser) {
            String json = "{\"email\":\"%s\",\"snsAccountId\":\"%s\",\"snsTypeId\":%d}"
                    .formatted(email, snsAccountId, snsTypeId);
            String encoded = URLEncoder.encode(json, StandardCharsets.UTF_8);

            ResponseCookie signupCookie = ResponseCookie.from("signupInfo", encoded)
                    .httpOnly(false)
                    .secure(false) // 운영 환경에선 true로 설정
                    .path("/")
                    .maxAge(Duration.ofMinutes(5))
                    .sameSite("Lax")
                    .build();
            response.addHeader("Set-Cookie", signupCookie.toString());
        }

        // ✅ 리디렉션 (프론트엔드에서 쿠키 확인 후 처리)
        response.sendRedirect("http://localhost:5173/oauth2/success");
    }
}
