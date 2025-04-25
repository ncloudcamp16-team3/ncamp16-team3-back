package tf.tailfriend.petsitter.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tf.tailfriend.global.config.UserPrincipal;
import tf.tailfriend.global.response.CustomResponse;
import tf.tailfriend.petsitter.dto.PetSitterRequestDto;
import tf.tailfriend.petsitter.dto.PetSitterResponseDto;
import tf.tailfriend.petsitter.entity.PetSitter;
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

            // 기존 펫시터 정보 확인
            // checkCurrentStatus 대신 existsById만 확인
            boolean exists = petSitterService.existsById(userPrincipal.getUserId());

            if (exists) {
                // 이미 존재하는 경우, 펫시터 정보 삭제 후 재등록
                try {

                    // 삭제 후 새로 등록
                    PetSitterResponseDto result = petSitterService.applyForPetSitter(requestDto, image);
                    return ResponseEntity.ok(new CustomResponse("펫시터 신청이 완료되었습니다. 관리자 승인 후 활동이 가능합니다.", result));
                } catch (Exception e) {
                    // 삭제 실패 시
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(new CustomResponse("이미 신청된 정보가 있습니다. 잠시 후 다시 시도해주세요.", null));
                }
            } else {
                // 존재하지 않는 경우 새로 등록
                PetSitterResponseDto result = petSitterService.applyForPetSitter(requestDto, image);
                return ResponseEntity.ok(new CustomResponse("펫시터 신청이 완료되었습니다. 관리자 승인 후 활동이 가능합니다.", result));
            }
        } catch (Exception e) {
            log.error("펫시터 신청 중 예외 발생: userId={}", userPrincipal.getUserId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CustomResponse("펫시터 신청 중 오류가 발생했습니다: " + e.getMessage(), null));
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
            // 먼저 펫시터 정보가 있는지 확인
            boolean exists = petSitterService.existsById(userPrincipal.getUserId());

            if (!exists) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new CustomResponse("펫시터 정보가 없습니다.", null));
            }

            PetSitterResponseDto result = petSitterService.getPetSitterStatus(userPrincipal.getUserId());
            return ResponseEntity.ok(new CustomResponse("조회 성공", result));

        } catch (IllegalArgumentException e) {
            // 펫시터 정보가 없는 경우
            log.info("펫시터 상태 조회 - 정보 없음: userId={}", userPrincipal.getUserId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new CustomResponse(e.getMessage(), null));

        } catch (Exception e) {
            log.error("펫시터 상태 조회 오류: userId={}", userPrincipal.getUserId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CustomResponse("펫시터 상태 조회 중 오류가 발생했습니다: " + e.getMessage(), null));
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
            log.info("펫시터 그만두기 요청: userId={}", userPrincipal.getUserId());
            petSitterService.quitPetSitter(userPrincipal.getUserId());
            log.info("펫시터 그만두기 성공: userId={}", userPrincipal.getUserId());

            return ResponseEntity.ok(new CustomResponse("펫시터 활동을 중단하였습니다.", null));

        } catch (IllegalArgumentException e) {
            log.warn("펫시터 그만두기 실패 - 유효성 오류: userId={}, message={}", userPrincipal.getUserId(), e.getMessage());
            return ResponseEntity.badRequest().body(new CustomResponse(e.getMessage(), null));

        } catch (Exception e) {
            log.error("펫시터 그만두기 오류: userId={}", userPrincipal.getUserId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CustomResponse("펫시터 그만두기 처리 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }
}