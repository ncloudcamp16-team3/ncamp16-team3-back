//
//package tf.tailfriend.global.config;
//
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
//import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
//import org.springframework.util.SerializationUtils;
//
//import java.util.Base64;
//
//public class HttpCookieOAuth2AuthorizationRequestRepository
//        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
//
//    public static final String OAUTH2_AUTH_REQUEST_COOKIE_NAME = "oauth2_auth_request";
//    public static final int COOKIE_EXPIRE_SECONDS = 180;
//
//    @Override
//    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
//        return CookieUtils.getCookie(request, OAUTH2_AUTH_REQUEST_COOKIE_NAME)
//                .map(cookie -> deserialize(cookie.getValue()))
//                .orElse(null);
//    }
//
//    @Override
//    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
//                                         HttpServletRequest request, HttpServletResponse response) {
//        if (authorizationRequest == null) {
//            CookieUtils.deleteCookie(request, response, OAUTH2_AUTH_REQUEST_COOKIE_NAME);
//            return;
//        }
//
//        String serialized = serialize(authorizationRequest);
//        CookieUtils.addCookie(response, OAUTH2_AUTH_REQUEST_COOKIE_NAME, serialized, COOKIE_EXPIRE_SECONDS);
//    }
//
//    @Override
//    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
//        return null;
//    }
//
//    private String serialize(OAuth2AuthorizationRequest object) {
//        return Base64.getUrlEncoder().encodeToString(SerializationUtils.serialize(object));
//    }
//
//    private OAuth2AuthorizationRequest deserialize(String cookie) {
//        return (OAuth2AuthorizationRequest) SerializationUtils.deserialize(Base64.getUrlDecoder().decode(cookie));
//    }
//}
