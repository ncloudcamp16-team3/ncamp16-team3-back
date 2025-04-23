package tf.tailfriend.petsitter.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tf.tailfriend.global.config.UserPrincipal;
import tf.tailfriend.global.response.CustomResponse;
import tf.tailfriend.petsitter.dto.PetSitterRequestDto;
import tf.tailfriend.petsitter.dto.PetSitterResponseDto;
import tf.tailfriend.petsitter.service.PetSitterService;

@RestController
@RequestMapping("/api/petsitter")
@RequiredArgsConstructor
@Slf4j
public class PetSitterController {

    private final PetSitterService petSitterService;

    /**
     * 사용자가 펫시터 신청을 제출하는 API
     */
    @PostMapping("/apply")
    public ResponseEntity<?> applyForPetSitter(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestPart("data") PetSitterRequestDto requestDto,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        if (userPrincipal == null) {
            return ResponseEntity.status(401).body(new CustomResponse("로그인이 필요합니다.", null));
        }

        try {
            // 사용자 ID 설정
            requestDto.setUserId(userPrincipal.getUserId());

            // 서비스 호출 - 상태는 서비스 내에서 PENDING으로 설정
            PetSitterResponseDto result = petSitterService.applyForPetSitter(requestDto, image);

            return ResponseEntity.ok(new CustomResponse("펫시터 신청이 완료되었습니다. 관리자 승인 후 활동이 가능합니다.", result));
        } catch (Exception e) {
            log.error("펫시터 신청 오류", e);
            return ResponseEntity.badRequest().body(new CustomResponse("펫시터 신청 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }

    /**
     * 사용자의 펫시터 신청 상태를 조회하는 API
     */
    @GetMapping("/status")
    public ResponseEntity<?> getPetSitterStatus(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return ResponseEntity.status(401).body(new CustomResponse("로그인이 필요합니다.", null));
        }

        try {
            PetSitterResponseDto result = petSitterService.getPetSitterStatus(userPrincipal.getUserId());

            return ResponseEntity.ok(new CustomResponse("조회 성공", result));
        } catch (Exception e) {
            log.error("펫시터 상태 조회 오류", e);
            return ResponseEntity.badRequest().body(new CustomResponse("펫시터 상태 조회 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }

    /**
     * 사용자가 펫시터를 그만두는 API
     */
    @PostMapping("/quit")
    public ResponseEntity<?> quitPetSitter(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return ResponseEntity.status(401).body(new CustomResponse("로그인이 필요합니다.", null));
        }

        try {
            petSitterService.quitPetSitter(userPrincipal.getUserId());

            return ResponseEntity.ok(new CustomResponse("펫시터 활동을 중단하였습니다.", null));
        } catch (Exception e) {
            log.error("펫시터 그만두기 오류", e);
            return ResponseEntity.badRequest().body(new CustomResponse("펫시터 그만두기 처리 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }
}