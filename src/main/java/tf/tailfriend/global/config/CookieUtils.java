package tf.tailfriend.global.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.util.SerializationUtils;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

public class CookieUtils {

    private static final int COOKIE_MAX_AGE = 180; // 초 단위 (예: 3분)
    private static final String ENCODING = "UTF-8";

    // 쿠키 가져오기
    public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return Optional.empty();
        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(name))
                .findFirst();
    }

    // 쿠키 값 가져오기 (디코딩 포함)
    public static Optional<String> getCookieValue(HttpServletRequest request, String name) {
        return getCookie(request, name)
                .map(cookie -> {
                    try {
                        return URLDecoder.decode(cookie.getValue(), ENCODING);
                    } catch (UnsupportedEncodingException e) {
                        return null;
                    }
                });
    }

    // 쿠키 추가
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        String osName = System.getProperty("os.name").toLowerCase();
        boolean isLinux = osName.contains("linux");

        // ✅ Base64로 인코딩된 value는 그대로 사용
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(isLinux)
                .path("/")
                .maxAge(maxAge)
                .sameSite(isLinux ? "None" : "Lax")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    // maxAge 기본값으로 쿠키 추가 (180초)
    public static void addCookie(HttpServletResponse response, String name, String value) {
        addCookie(response, name, value, COOKIE_MAX_AGE);
    }

    // 쿠키 삭제
    public static void deleteCookie(HttpServletResponse response, String name) {
        String osName = System.getProperty("os.name").toLowerCase();
        boolean isLinux = osName.contains("linux");

        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(isLinux)
                .path("/")
                .maxAge(0) // 쿠키 삭제
                .sameSite(isLinux ? "None" : "Lax")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

}
