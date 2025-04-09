package tf.tailfriend.user.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tf.tailfriend.user.entity.Users;
import tf.tailfriend.user.entity.dto.LoginRequestDto;
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

        System.out.println("🔥 register() called!");
        System.out.println("📦 DTO received: " + dto);
        // 받은 JSON payload 디버깅 로그 출력
        logger.debug("Received registration request: {}", dto);

        Users savedUser = userService.registerUser(dto);
        logger.debug("Saved user: {}", savedUser);

        String token = authService.login(new LoginRequestDto(savedUser.getSnsAccountId()));
        logger.debug("Generated token: {}", token);

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
