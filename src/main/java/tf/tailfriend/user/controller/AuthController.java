package tf.tailfriend.user.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tf.tailfriend.user.entity.Users;
import tf.tailfriend.user.entity.dto.LoginRequestDto;
import tf.tailfriend.user.entity.dto.NewDto;
import tf.tailfriend.user.entity.dto.UserRegisterDto;
import tf.tailfriend.user.service.AuthService;
import tf.tailfriend.user.service.UserService;

import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/register")
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


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto dto) {
        logger.debug("Received login request: {}", dto);
        String token = authService.login(dto);
        logger.debug("Generated token: {}", token);
        return ResponseEntity.ok(Collections.singletonMap("token", token));
    }
}
