package tf.tailfriend.user.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tf.tailfriend.user.config.UserPrincipal;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserPrincipal user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getUserId());
        result.put("email", user.getEmail());
        result.put("snsTypeId", user.getSnsTypeId());

        return ResponseEntity.ok(result);
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0) // 쿠키 만료
                .sameSite("Lax")
                .build();

        response.setHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok("Logged out");
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkLogin(@AuthenticationPrincipal UserPrincipal user) {
        Map<String, Object> result = new HashMap<>();

        if (user == null) {
            result.put("loggedIn", false);
            return ResponseEntity.status(401).body(result);
        }

        result.put("loggedIn", true);
        result.put("userId", user.getUserId());
        result.put("email", user.getEmail());
        result.put("snsTypeId", user.getSnsTypeId());

        return ResponseEntity.ok(result);
    }

}

