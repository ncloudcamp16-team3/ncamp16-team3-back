package tf.tailfriend.user.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tf.tailfriend.global.config.JwtAuthenticationFilter;
import tf.tailfriend.global.config.JwtTokenProvider;
import tf.tailfriend.global.config.UserPrincipal;
import tf.tailfriend.global.service.NCPObjectStorageService;
import tf.tailfriend.global.service.StorageService;
import tf.tailfriend.user.entity.User;
import tf.tailfriend.user.entity.dto.LoginRequestDto;
import tf.tailfriend.user.entity.dto.RegisterUserDto;
import tf.tailfriend.user.entity.dto.UserInfoDto;
import tf.tailfriend.user.repository.UserDao;
import tf.tailfriend.user.service.AuthService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);


//    @GetMapping("/csrf")
//    public Map<String, String> getCsrfToken(CsrfToken csrfToken) {
//        Map<String, String> token = new HashMap<>();
//        token.put("csrfToken", csrfToken.getToken());
//        return token;
//    }


    // ✅ 유저 상세정보 조회
    @GetMapping("/userinfo")
    public ResponseEntity<?> getUserInfo(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증되지 않은 사용자입니다.");
        }

        Integer userId = userPrincipal.getUserId();
        UserInfoDto userInfo = authService.getUserInfoById(userId);
        System.out.println(userInfo);

        return ResponseEntity.ok(userInfo);
    }


    @PostMapping(value="/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> register(@RequestPart("dto") RegisterUserDto dto,
                                      @RequestPart(value = "images", required = false) List<MultipartFile> images,
                                      HttpServletResponse response) {
        logger.info("🔥 register() called!");
        logger.debug("📦 DTO received: {}", dto);

        if (images == null) {
            images = new ArrayList<>(); // null 방지
        }
        // 유저 등록
        User savedUser = authService.registerUser(dto,images); // 반환값 Users로 변경

        boolean isNewUser = savedUser == null;

        String token = jwtTokenProvider.createToken(
                savedUser.getId(),
                savedUser.getSnsAccountId(),
                savedUser.getSnsType().getId(),
                isNewUser
        );
        logger.debug("🔐 Generated token: {}", token);

        response.addHeader("Set-Cookie", createJwtCookie(token).toString());
        response.addHeader("Set-Cookie", clearCookie("signupInfo").toString());

        return ResponseEntity.ok(Map.of("message", "회원가입 및 로그인 성공"));
    }




    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        String osName = System.getProperty("os.name").toLowerCase();
        System.out.println("/api/auth/logout : "+osName);
        response.addHeader("Set-Cookie", clearCookie("accessToken").toString());
        return ResponseEntity.ok(Map.of("message", "로그아웃 완료"));
    }


    @GetMapping("/check")
    public ResponseEntity<?> checkLogin(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("loggedIn", false));
        }

        Map<String, Object> response = new HashMap<>();

        response.put("isNewUser", userPrincipal.getIsNewUser());
        response.put("userId",  userPrincipal.getUserId());
        response.put("snsAccountId", userPrincipal.getSnsAccountId());
        response.put("snsTypeId", userPrincipal.getSnsTypeId());


        return ResponseEntity.ok(response);
    }



    private ResponseCookie createJwtCookie(String token) {
        String osName = System.getProperty("os.name").toLowerCase();
        System.out.println("create Cookie : " + osName);

        boolean isLinux = osName.contains("linux");

        return ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                .secure(isLinux) // 리눅스(서버)일 경우 secure true
                .path("/")
                .maxAge(Duration.ofDays(1))
                .sameSite(isLinux ? "None" : "Lax")
                .build();
    }

    // 🔧 쿠키 삭제 (0초로 만료)
    private ResponseCookie clearCookie(String name) {
        String osName = System.getProperty("os.name").toLowerCase();
        System.out.println("create Cookie : " + osName);

        boolean isLinux = osName.contains("linux");


        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(isLinux) // 리눅스(서버)일 경우 secure true
                .path("/")
                .maxAge(0)
                .sameSite(isLinux ? "None" : "Lax")
                .build();
    }

}


