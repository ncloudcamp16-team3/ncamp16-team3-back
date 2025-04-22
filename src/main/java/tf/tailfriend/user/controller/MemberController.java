package tf.tailfriend.user.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tf.tailfriend.global.config.UserPrincipal;
import tf.tailfriend.user.entity.dto.MypageResponseDto;
import tf.tailfriend.user.service.MemberService;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private static final Logger logger = LoggerFactory.getLogger(MemberController.class);
    private final MemberService memberService;

    /**
     * 마이페이지 정보 조회 API
     */
    @GetMapping("/mypage")
    public ResponseEntity<?> getMyPageInfo(@AuthenticationPrincipal UserPrincipal principal) {
        logger.info("마이페이지 정보 조회 요청");

        if (principal == null) {
            logger.error("인증되지 않은 요청");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다."));
        }

        try {
            MypageResponseDto response = memberService.getMemberInfo(principal.getUserId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("마이페이지 정보 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "정보를 조회하는 중 오류가 발생했습니다."));
        }
    }

    /**
     * 닉네임 업데이트 API
     */
    @PutMapping("/nickname")
    public ResponseEntity<?> updateNickname(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> request) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다."));
        }

        String newNickname = request.get("nickname");
        if (newNickname == null || newNickname.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "닉네임은 필수 입력값입니다."));
        }

        try {
            String updatedNickname = memberService.updateNickname(principal.getUserId(), newNickname);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "닉네임이 성공적으로 변경되었습니다.");
            response.put("nickname", updatedNickname);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "닉네임을 변경하는 중 오류가 발생했습니다."));
        }
    }

    /**
     * 회원탈퇴 API
     */
    @DeleteMapping("/withdraw")
    public ResponseEntity<?> withdraw(@AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다."));
        }
        try {
            memberService.withdrawMember(principal.getUserId());
            return ResponseEntity.ok(Map.of("message", "회원 탈퇴가 완료되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "회원 탈퇴 처리 중 오류가 발생했습니다.."));
        }
    }


    /**
     * 프로필 이미지 업데이트 API
     */
    @PutMapping("/profile-image")
    public ResponseEntity<?> updateProfileImage(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Integer> request) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다."));
        }

        Integer fileId = request.get("fileId");
        if (fileId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "파일 ID는 필수 입력값입니다."));
        }

        try {
            String imageUrl = memberService.updateProfileImage(principal.getUserId(), fileId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "프로필 이미지가 성공적으로 변경되었습니다.");
            response.put("profileImageUrl", imageUrl);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "프로필 이미지를 변경하는 중 오류가 발생했습니다."));
        }
    }


    @PostMapping("/{postId}/follow")
    public ResponseEntity<String> toggleBookmark(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("postId") Integer postId
    ) {
        Integer userId = userPrincipal.getUserId();
        return ResponseEntity.ok("팔로우 토글 완료");
    }

}