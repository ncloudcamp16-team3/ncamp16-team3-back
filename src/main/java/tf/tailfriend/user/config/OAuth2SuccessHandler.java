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

        System.out.println("OAuth2User attributes = " + oAuth2User.getAttributes());

        var attributes = oAuth2User.getAttributes();

        String email = OAuth2AttributeExtractor.getEmail(attributes);
        String snsAccountId = OAuth2AttributeExtractor.getSnsAccountId(attributes);
        Integer snsTypeId = OAuth2AttributeExtractor.getSnsTypeId(attributes);

        Integer userId = userService.getUserIdBySnsAccountId(snsAccountId);
        boolean isNewUser = (userId == null);
        if (isNewUser) {
            userId = -1;
        }

        String token = jwtTokenProvider.createToken(userId, email, snsTypeId);

        ResponseCookie cookie = ResponseCookie.from("accessToken", token)
                .httpOnly(true)                     // JS 접근 불가
                .secure(false)                     // HTTPS 환경이면 true로
                .path("/")                         // 모든 경로에 전송
                .maxAge(Duration.ofDays(1))        // 1일 유효
                .sameSite("Lax")                   // SameSite 설정 (Strict/Lax/None)
                .build();             // 1일 유효

        response.addHeader("Set-Cookie", cookie.toString());

//        String script = """
//            <html><body>
//            <script>
//              window.opener.postMessage({
//                type: 'OAUTH_SUCCESS',
//                isNewUser: %s
//              }, '*');
//              window.close();
//            </script>
//            로그인 처리 중입니다...
//            </body></html>
//            """.formatted(isNewUser);
//
//        response.setContentType("text/html;charset=UTF-8");
//        response.getWriter().write(script);

        // ✅ 리다이렉트 방식으로 성공 페이지 이동
        response.sendRedirect("http://localhost:5173/oauth2/success?new=" + isNewUser);

    }
}
