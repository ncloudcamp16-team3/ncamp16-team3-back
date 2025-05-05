package tf.tailfriend.user.controller;


import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tf.tailfriend.global.config.CookieUtils;
import tf.tailfriend.global.config.JwtTokenProvider;
import tf.tailfriend.global.config.UserPrincipal;
import tf.tailfriend.user.entity.User;
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

        CookieUtils.addCookie(response, "accessToken", token, 60 * 60 * 24); // 1일짜리

        return ResponseEntity.ok(Map.of("message", "회원가입 및 로그인 성공"));
    }




    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {


        CookieUtils.deleteCookie(response, "accessToken");
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

    @GetMapping("/check-nickname")
    public ResponseEntity<Map<String, Boolean>> checkNickname(@RequestParam String nickname) {
        boolean exists = authService.isNicknameExists(nickname);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }


}


