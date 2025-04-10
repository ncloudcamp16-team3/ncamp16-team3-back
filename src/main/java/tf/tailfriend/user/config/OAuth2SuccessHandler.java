//package tf.tailfriend.user.config;
//
//import jakarta.servlet.http.Cookie;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
//import org.springframework.stereotype.Component;
//import org.springframework.web.util.UriComponentsBuilder;
//
//import java.io.IOException;
//import java.util.Map;
//
//@Component
//public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)  throws IOException {
//        //authentication에 OAuth 토큰 발급 후 전달 받은 사용자 정보가 들어있다.
//        //토큰 발급 요청 부터 사용자 정보 요청까지 Spring Security가 내부적으로 진행한다.
//
//        //사용자 정보 추출
//        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
//        Map<String, Object> attributes = oAuth2User.getAttributes();
//
//        //사용자 정보를 전달 또는 JWT 생성 후 전달
//        //일반적으로는 사용자 정보를 바탕으로 JWT를 생성하고 전달한다.
//        Cookie cookie = new Cookie("name", (String) attributes.get("name"));
//        cookie.setPath("/");
//        response.addCookie(cookie);
//
//        String targetUrl = "http://localhost:5173"; //redirect 시킬 react 경로
//        String redirectUrl = UriComponentsBuilder.fromUriString(targetUrl)
//                .build().toUriString();
//
//        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
//    }
//}

/// //////////////////////////////////
//package tf.tailfriend.user.config;
//
//import jakarta.servlet.http.Cookie;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
//import org.springframework.stereotype.Component;
//import org.springframework.web.util.UriComponentsBuilder;
//
//import java.io.IOException;
//import java.util.Map;
//
//@Component
//public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)  throws IOException {
//        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
//        Map<String, Object> attributes = oAuth2User.getAttributes();
//
//        String email = getEmail(attributes);
//
//        // 이메일 정보를 쿠키에 저장
//        Cookie emailCookie = new Cookie("email", email);
//        emailCookie.setPath("/");
//        emailCookie.setHttpOnly(true);
//        response.addCookie(emailCookie);
//
//        // React 클라이언트로 리디렉트
//        String targetUrl = "http://localhost:5173/register";
//        String redirectUrl = UriComponentsBuilder.fromUriString(targetUrl)
//                .build().toUriString();
//
//        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
//    }
//
//    // 각 OAuth 제공자별로 이메일 정보 가져오는 메서드
//    private String getEmail(Map<String, Object> attributes) {
//        if (attributes.containsKey("email")) {
//            return (String) attributes.get("email"); // Google, 일반적인 OAuth2에서 사용
//        } else if (attributes.containsKey("kakao_account")) {
//            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
//            return (String) kakaoAccount.get("email");
//        } else if (attributes.containsKey("response")) {
//            Map<String, Object> naverResponse = (Map<String, Object>) attributes.get("response");
//            return (String) naverResponse.get("email");
//        }
//        return "unknown";
//    }
package tf.tailfriend.user.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import tf.tailfriend.user.entity.dto.OAuth2LoginInfo;
import tf.tailfriend.user.service.UserService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final OAuth2SessionStorage oAuth2SessionStorage;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = OAuth2AttributeExtractor.getEmail(attributes);
        String snsAccountId = OAuth2AttributeExtractor.getSnsAccountId(attributes);
        Integer snsTypeId = OAuth2AttributeExtractor.getSnsTypeId(attributes);

        Integer userId = userService.getUserIdBySnsAccountId(snsAccountId);
        if (userId == null) {
            userId = -1;
        }

        String token = jwtTokenProvider.createToken(userId);

        // 👉 OAuth2LoginInfo 객체 생성
        OAuth2LoginInfo loginInfo = new OAuth2LoginInfo();
        loginInfo.setToken(token);
        loginInfo.setEmail(email);
        loginInfo.setSnsTypeId(snsTypeId);
        loginInfo.setSnsAccountId(snsAccountId);

        // 👉 세션 ID로 저장
        String sessionId = oAuth2SessionStorage.save(loginInfo);

        // 3. 프론트로 리디렉트
        String redirectUrl = UriComponentsBuilder
                .fromUriString("http://localhost:5173/register")
                .queryParam("sessionId", sessionId)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}