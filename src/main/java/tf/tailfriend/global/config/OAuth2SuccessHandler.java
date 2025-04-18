package tf.tailfriend.global.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tf.tailfriend.user.service.UserService;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;


    @Value("${URL}")
    private String mainUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        var attributes = oAuth2User.getAttributes();

        // 🔍 사용자 정보 추출
        String snsAccountId = OAuth2AttributeExtractor.getSnsAccountId(attributes);
        Integer snsTypeId = OAuth2AttributeExtractor.getSnsTypeId(attributes);

        // 🟡 가입 여부 확인
        Integer userId = userService.getUserIdBySnsAccountId(snsAccountId);
        boolean isNewUser = (userId == null);
        if (isNewUser) {
            userId = -1; // DB에 아직 없는 유저
        }
        // 🔐 JWT 생성
        String token = jwtTokenProvider.createToken(userId, snsAccountId, snsTypeId, isNewUser);

        if (mainUrl.equals("http://localhost:5173")) {
            // 🍪 accessToken 쿠키 설정
            ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", token)
                    .httpOnly(true)
                    .secure(false) // 👉 배포 시 반드시 true
                    .path("/")
                    .maxAge(Duration.ofHours(1))
                    .sameSite("Lax")
                    .build();
            response.addHeader("Set-Cookie", accessTokenCookie.toString());
        }
        else{
            ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", token)
                    .httpOnly(true)
                    .secure(true) // 👉 배포 시 반드시 true
                    .path("/")
                    .maxAge(Duration.ofHours(1))
                    .sameSite("None")
                    .build();
            response.addHeader("Set-Cookie", accessTokenCookie.toString());
        }


        String redirectUrl = mainUrl+"/oauth2/success";
        System.out.println(redirectUrl);
        response.sendRedirect(redirectUrl);

    }

}