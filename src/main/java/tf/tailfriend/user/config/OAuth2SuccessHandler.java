package tf.tailfriend.user.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tf.tailfriend.user.service.UserService;
import jakarta.servlet.http.Cookie;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

//@Component
//@RequiredArgsConstructor
//public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
//
//    private final JwtTokenProvider jwtTokenProvider;
//    private final UserService userService;
//
//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request,
//                                        HttpServletResponse response,
//                                        Authentication authentication) throws IOException {
//        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
//        Map<String, Object> attributes = oAuth2User.getAttributes();
//
//        String email = OAuth2AttributeExtractor.getEmail(attributes);
//        String snsAccountId = OAuth2AttributeExtractor.getSnsAccountId(attributes);
//        Integer snsTypeId = OAuth2AttributeExtractor.getSnsTypeId(attributes);
//
//        Integer userId = userService.getUserIdBySnsAccountId(snsAccountId);
//        if (userId == null) {
//            userId = -1;
//        }
//
//        String token = jwtTokenProvider.createToken(userId, email, snsTypeId.toString());
//
//
//        String targetOrigin = "http://localhost:5173";
//
//        Map<String, Object> message = new HashMap<>();
//        message.put("token", token);
//        message.put("userId", userId);
//        message.put("email", email);
//        message.put("snsTypeId", snsTypeId);
//        message.put("isRegistered", userId != -1);
//
//        String json = new ObjectMapper().writeValueAsString(message);
//
//        String html = "<html><body><script>" +
//                "window.opener.postMessage(" + json + ", '" + targetOrigin + "');" +
//                "window.close();" +
//                "</script></body></html>";
//
//        response.setContentType("text/html;charset=UTF-8");
//        response.getWriter().write(html);
//    }
//}



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

        Integer userId = userService.getUserIdBySnsAccountId(snsAccountId);
        boolean isNewUser = (userId == null);
        if (isNewUser) {
            userId = -1;
        }

        String token = jwtTokenProvider.createToken(userId, email, snsTypeId);

        // ✅ JWT를 HttpOnly 쿠키에 저장
        Cookie cookie = new Cookie("accessToken", token);
        cookie.setHttpOnly(true);                    // JS 접근 불가
        cookie.setSecure(false);
        cookie.setPath("/");                         // 모든 경로에 전송
        cookie.setMaxAge(60 * 60 * 24);              // 1일 유효

        response.addCookie(cookie);

        // ✅ 팝업 내 HTML 반환 (JS로 부모창에 메시지 전송 + 팝업 닫기)
        String script = """
            <html><body>
            <script>
              window.opener.postMessage({
                type: 'OAUTH_SUCCESS',
                isNewUser: %s
              }, '*');
              window.close();
            </script>
            로그인 처리 중입니다...
            </body></html>
            """.formatted(isNewUser);

        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(script);

    }
}
