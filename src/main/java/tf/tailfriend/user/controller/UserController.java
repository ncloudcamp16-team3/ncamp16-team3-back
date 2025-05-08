package tf.tailfriend.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tf.tailfriend.file.entity.File;
import tf.tailfriend.file.service.FileService;
import tf.tailfriend.global.config.UserPrincipal;
import tf.tailfriend.global.response.CustomResponse;
import tf.tailfriend.global.service.StorageService;
import tf.tailfriend.global.service.StorageServiceException;
import tf.tailfriend.petsitter.dto.PetSitterResponseDto;
import tf.tailfriend.petsitter.service.PetSitterService;
import tf.tailfriend.user.entity.dto.MypageResponseDto;
import tf.tailfriend.user.entity.dto.UserInfoDto;
import tf.tailfriend.user.service.UserService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tf.tailfriend.user.message.SuccessMessage.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final PetSitterService petSitterService;
    private final FileService fileService;
    private final StorageService storageService;

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
            MypageResponseDto response = userService.getMemberInfo(principal.getUserId());

            // API에서 받아온 데이터로 응답 맵 구성
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("userId", response.getUserId());
            responseMap.put("nickname", response.getNickname());
            responseMap.put("profileImageUrl", response.getProfileImageUrl());
            responseMap.put("pets", response.getPets());

            try {
                boolean exists = petSitterService.existsById(principal.getUserId());
                if (exists) {
                    PetSitterResponseDto sitterDto = petSitterService.getPetSitterStatus(principal.getUserId());

                    // 상태값을 그대로 전달 (이미 String으로 변환되어 있음)
                    responseMap.put("petSitterStatus", sitterDto.getStatus());
                    responseMap.put("petSitterInfo", sitterDto);

                    logger.info("펫시터 상태 조회 성공: userId={}, status={}",
                            principal.getUserId(), sitterDto.getStatus());
                } else {
                    responseMap.put("petSitterStatus", "NOT_REGISTERED");
                    logger.info("펫시터 정보 없음: userId={}", principal.getUserId());
                }
            } catch (Exception e) {
                // 펫시터가 아닌 경우
                logger.warn("펫시터 상태 조회 중 오류: userId={}, error={}", principal.getUserId(), e.getMessage());
                responseMap.put("petSitterStatus", "NOT_REGISTERED");
            }

            return ResponseEntity.ok(responseMap);
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
            String updatedNickname = userService.updateNickname(principal.getUserId(), newNickname);
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
            List<String> channelNames = userService.withdrawMember(principal.getUserId());

            return ResponseEntity.ok(Map.of(
                    "message", "회원 탈퇴가 완료되었습니다.",
                    "channelNames", channelNames // ✅ 프론트에서 이걸로 채널 삭제 가능
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "회원 탈퇴 처리 중 오류가 발생했습니다."));
        }
    }


    /**
     * 프로필 이미지 업로드 API
     */
    @PostMapping(value = "/profile-image", consumes = "multipart/form-data")
    public ResponseEntity<?> updateProfileImage(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("image") MultipartFile imageFile) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다."));
        }

        // 파일 유효성 검사
        if (imageFile == null || imageFile.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "이미지 파일이 필요합니다."));
        }

        // 이미지 파일 확인
        if (!imageFile.getContentType().startsWith("image/")) {
            return ResponseEntity.badRequest().body(Map.of("error", "이미지 파일만 업로드 가능합니다."));
        }

        try {
            // 1. 파일 메타데이터 저장
            File file = fileService.save(imageFile.getOriginalFilename(), "profiles", File.FileType.PHOTO);

            // 2. 파일 스토리지에 업로드
            try (InputStream inputStream = imageFile.getInputStream()) {
                storageService.upload(file.getPath(), inputStream);
            } catch (StorageServiceException e) {
                log.error("파일 스토리지 업로드 실패", e);
                return ResponseEntity.internalServerError()
                        .body(Map.of("error", "프로필 이미지 저장에 실패했습니다."));
            }

            // 3. 유저 프로필 이미지 업데이트
            String imageUrl = userService.updateProfileImage(principal.getUserId(), file.getId());

            // 4. 응답 반환
            Map<String, Object> response = new HashMap<>();
            response.put("message", "프로필 이미지가 성공적으로 변경되었습니다.");
            response.put("profileImageUrl", storageService.generatePresignedUrl(imageUrl));

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("프로필 이미지 업로드 실패", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "프로필 이미지 처리 중 오류가 발생했습니다."));
        } catch (Exception e) {
            log.error("프로필 이미지 업데이트 실패", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "프로필 이미지 업데이트 중 오류가 발생했습니다."));
        }
    }


    // 기존 프로필 이미지 업데이트 API
    @PutMapping("/profile-image")
    public ResponseEntity<?> updateProfileImageById(
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
            String imageUrl = userService.updateProfileImage(principal.getUserId(), fileId);
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

    @PostMapping("/{userId}/follow")
    public ResponseEntity<String> toggleFollow(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("userId") Integer userId
    ) {
        Integer followerId = userPrincipal.getUserId();
        userService.toggleFollow(followerId, userId);

        return ResponseEntity.ok("팔로우 토글 완료");
    }

    @PutMapping("/save")
    public ResponseEntity<?> save(@RequestBody UserInfoDto userInfoDto) {
        userService.userInfoSave(userInfoDto);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new CustomResponse(USER_INFO_SAVE_SUCCESS.getMessage(), null));
    }


}