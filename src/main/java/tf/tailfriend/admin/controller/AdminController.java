package tf.tailfriend.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tf.tailfriend.admin.dto.AdminLoginRequest;
import tf.tailfriend.admin.dto.AdminLoginResponse;
import tf.tailfriend.admin.entity.Admin;
import tf.tailfriend.admin.service.AdminService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AdminLoginRequest request) {
        Admin admin = adminService.register(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(Map.of("message", "관리자 등록 성공!", "email", admin.getEmail()));
    }

    @GetMapping("/auth/validate")
    public ResponseEntity<Map<String, Object>> validateToken(Authentication authentication) {
        log.info("Validating token");
        if (authentication != null && authentication.isAuthenticated()) {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("email", authentication.getName());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("valid", false));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AdminLoginRequest request) {
        log.info("Login request: {}", request);
        AdminLoginResponse response = adminService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authorizationHeader) {
        log.info("Logout request: {}", authorizationHeader);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            log.info("Logout request");

            try {
                adminService.logout(token);
                return ResponseEntity.ok(Map.of("message", "로그아웃 성공"));
            } catch (Exception e) {
                log.error("로그아웃 처리 중 오류 발생: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "로그아웃 처리 중 오류 발생"));
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "인증토큰이 없습니다"));
    }
}
