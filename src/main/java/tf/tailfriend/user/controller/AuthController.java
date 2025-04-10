package tf.tailfriend.user.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import tf.tailfriend.user.config.JwtTokenProvider;
import tf.tailfriend.user.config.OAuth2AttributeExtractor;
import tf.tailfriend.user.config.OAuth2SessionStorage;
import tf.tailfriend.user.entity.dto.LoginRequestDto;
import tf.tailfriend.user.entity.dto.OAuth2LoginInfo;
import tf.tailfriend.user.entity.dto.UserRegisterDto;
import tf.tailfriend.user.service.AuthService;
import tf.tailfriend.user.service.UserService;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final AuthService authService;
    private final OAuth2SessionStorage oauth2SessionStorage;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/api/auth/register")
    public ResponseEntity<?> register(@RequestBody UserRegisterDto dto) {
        logger.info("🔥 register() called!");
        logger.debug("📦 DTO received: {}", dto);

        // 유저 등록 (반환 없음)
        userService.registerUser(dto);

        // 로그인 처리 (snsAccountId로 로그인 요청)
        String token = authService.login(new LoginRequestDto(dto.getSnsAccountId()));
        logger.debug("🔐 Generated token: {}", token);

        return ResponseEntity.ok(Collections.singletonMap("token", token));
    }


    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto dto) {
        logger.debug("Received login request: {}", dto);
        String token = authService.login(dto);
        logger.debug("Generated token: {}", token);
        return ResponseEntity.ok(Collections.singletonMap("token", token));
    }


    @GetMapping("/oauth2/success")
    public void handleOAuth2Success(@AuthenticationPrincipal OAuth2User oAuth2User, HttpServletResponse response) throws IOException {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = OAuth2AttributeExtractor.getEmail(attributes);
        String snsAccountId = OAuth2AttributeExtractor.getSnsAccountId(attributes);
        Integer snsTypeId = OAuth2AttributeExtractor.getSnsTypeId(attributes);
        Integer userId = userService.getUserIdBySnsAccountId(snsAccountId);
        if (userId == null) userId = -1;

        String token = jwtTokenProvider.createToken(userId);

        OAuth2LoginInfo loginInfo = new OAuth2LoginInfo(email, snsAccountId, snsTypeId, token);
        String sessionId = oauth2SessionStorage.save(loginInfo);

        // 프론트로 sessionId만 전달
        response.sendRedirect("http://localhost:5173/register?sessionId=" + sessionId);
    }

    @GetMapping("/api/auth/oauth2/session")
    public ResponseEntity<?> getOAuth2Session(@RequestParam String sessionId) {
        OAuth2LoginInfo info = oauth2SessionStorage.get(sessionId);

        if (info == null) {
            return ResponseEntity.status(410).body("세션이 만료되었거나 존재하지 않습니다.");
        }

        // 한번 조회 후 제거 (1회용)
        oauth2SessionStorage.remove(sessionId);

        return ResponseEntity.ok(info);
    }


}


