package tf.tailfriend.user.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import tf.tailfriend.user.config.JwtTokenProvider;
import tf.tailfriend.user.config.OAuth2AttributeExtractor;
import tf.tailfriend.user.entity.Users;
import tf.tailfriend.user.entity.dto.LoginRequestDto;
import tf.tailfriend.user.entity.dto.UserRegisterDto;
import tf.tailfriend.user.service.AuthService;
import tf.tailfriend.user.service.UserService;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final AuthService authService;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/api/auth/register")
    public ResponseEntity<?> register(@RequestBody UserRegisterDto dto, HttpServletResponse response) {
        logger.info("🔥 register() called!");
        logger.debug("📦 DTO received: {}", dto);

        // 유저 등록
        userService.registerUser(dto);

        // 로그인 처리 → JWT 생성
        String token = authService.login(new LoginRequestDto(dto.getSnsAccountId()));
        logger.debug("🔐 Generated token: {}", token);

        // ✅ JWT를 HttpOnly 쿠키로 추가
        ResponseCookie cookie = ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofDays(1))
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        // ✅ signupInfo 쿠키 삭제
        ResponseCookie clearSignupInfoCookie = ResponseCookie.from("signupInfo", "")
                .httpOnly(false)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", clearSignupInfoCookie.toString());

        return ResponseEntity.ok(Collections.singletonMap("message", "회원가입 및 로그인 성공"));
    }


    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto dto) {
        logger.debug("Received login request: {}", dto);

        String token = authService.login(dto);
        logger.debug("Generated token: {}", token);

        return ResponseEntity.ok(Collections.singletonMap("token", token));
    }


    @GetMapping("/oauth2/success")
    public ResponseEntity<?> handleOAuth2Success(@AuthenticationPrincipal OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = OAuth2AttributeExtractor.getEmail(attributes);
        String snsAccountId = OAuth2AttributeExtractor.getSnsAccountId(attributes);
        Integer snsTypeId = OAuth2AttributeExtractor.getSnsTypeId(attributes);

        Users user = userService.findBySnsAccountId(snsAccountId).orElse(null);
        Integer userId = user != null ? user.getId() : -1;

        // JWT 생성
        String token = jwtTokenProvider.createToken(userId, email, snsTypeId);

        // 프론트에 필요한 정보 응답
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("email", email);
        response.put("snsAccountId", snsAccountId);
        response.put("snsTypeId", snsTypeId);
        response.put("userId", userId);

        return ResponseEntity.ok(response);
    }



}


