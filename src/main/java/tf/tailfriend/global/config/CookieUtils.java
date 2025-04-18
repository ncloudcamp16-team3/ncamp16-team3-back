//package tf.tailfriend.global.config;
//
//import jakarta.servlet.http.Cookie;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//
//import java.io.UnsupportedEncodingException;
//import java.net.URLDecoder;
//import java.net.URLEncoder;
//import java.util.Arrays;
//import java.util.Optional;
//
//public class CookieUtils {
//
//    private static final int COOKIE_MAX_AGE = 180; // 초 단위 (예: 3분)
//    private static final String ENCODING = "UTF-8";
//
//    // 쿠키 가져오기
//    public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
//        if (request.getCookies() == null) return Optional.empty();
//        return Arrays.stream(request.getCookies())
//                .filter(cookie -> cookie.getName().equals(name))
//                .findFirst();
//    }
//
//    // 쿠키 값 가져오기 (디코딩 포함)
//    public static Optional<String> getCookieValue(HttpServletRequest request, String name) {
//        return getCookie(request, name)
//                .map(cookie -> {
//                    try {
//                        return URLDecoder.decode(cookie.getValue(), ENCODING);
//                    } catch (UnsupportedEncodingException e) {
//                        return null;
//                    }
//                });
//    }
//
//    // 쿠키 추가
//    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
//        try {
//            String encodedValue = URLEncoder.encode(value, ENCODING);
//            Cookie cookie = new Cookie(name, encodedValue);
//            cookie.setPath("/");
//            cookie.setHttpOnly(true);
//            cookie.setMaxAge(maxAge);
//            response.addCookie(cookie);
//        } catch (UnsupportedEncodingException e) {
//            throw new RuntimeException("Failed to encode cookie value", e);
//        }
//    }
//
//    // maxAge 기본값으로 쿠키 추가 (180초)
//    public static void addCookie(HttpServletResponse response, String name, String value) {
//        addCookie(response, name, value, COOKIE_MAX_AGE);
//    }
//
//    // 쿠키 삭제
//    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
//        getCookie(request, name).ifPresent(cookie -> {
//            cookie.setValue("");
//            cookie.setPath("/");
//            cookie.setMaxAge(0);
//            response.addCookie(cookie);
//        });
//    }
//}
