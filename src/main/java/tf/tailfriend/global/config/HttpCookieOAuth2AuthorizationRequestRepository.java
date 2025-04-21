package tf.tailfriend.global.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.util.SerializationUtils;

import java.util.Base64;

public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String OAUTH2_AUTH_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    public static final int COOKIE_EXPIRE_SECONDS = 180;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return CookieUtils.getCookie(request, OAUTH2_AUTH_REQUEST_COOKIE_NAME)
                .map(cookie -> deserialize(cookie.getValue()))
                .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            CookieUtils.deleteCookie(response, OAUTH2_AUTH_REQUEST_COOKIE_NAME);
            return;
        }

        String serialized = serialize(authorizationRequest);
        CookieUtils.addCookie(response, OAUTH2_AUTH_REQUEST_COOKIE_NAME, serialized, COOKIE_EXPIRE_SECONDS);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
        CookieUtils.deleteCookie(response, OAUTH2_AUTH_REQUEST_COOKIE_NAME);
        return authorizationRequest;
    }

    private String serialize(OAuth2AuthorizationRequest object) {
        try {
            // Serialize the object into a byte array using SerializationUtils
            byte[] serializedObject = SerializationUtils.serialize(object);
            // Base64 encode the byte array for safe cookie storage
            return Base64.getUrlEncoder().encodeToString(serializedObject);
        } catch (Exception e) {
            throw new IllegalArgumentException("OAuth2AuthorizationRequest serialization failed", e);
        }
    }

    private OAuth2AuthorizationRequest deserialize(String cookie) {
        try {
            // Base64 decode
            byte[] decodedBytes = Base64.getUrlDecoder().decode(cookie);
            // Deserialize using SerializationUtils
            return (OAuth2AuthorizationRequest) SerializationUtils.deserialize(decodedBytes);
        } catch (Exception e) {
            // Enhanced error logging
            System.err.println("Error during deserialization: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalArgumentException("OAuth2AuthorizationRequest deserialization failed", e);
        }
    }
}
