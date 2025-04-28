package tf.tailfriend.petsitter.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tf.tailfriend.global.config.UserPrincipal;
import tf.tailfriend.global.response.CustomResponse;
import tf.tailfriend.petsitter.dto.PetSitterRequestDto;
import tf.tailfriend.petsitter.dto.PetSitterResponseDto;
import tf.tailfriend.petsitter.service.PetSitterService;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/petsitter")
@RequiredArgsConstructor
@Slf4j
public class PetSitterController {

    private final PetSitterService petSitterService;
    private final ObjectMapper objectMapper;

    /**
     * 사용자가 펫시터 신청을 제출하는 API
     */
    @PostMapping("/apply")
    public ResponseEntity<?> applyForPetSitter(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestPart("data") String requestDtoJson,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new CustomResponse("로그인이 필요합니다.", null));
        }

        try {
            // JSON 문자열을 DTO로 변환
            PetSitterRequestDto requestDto = objectMapper.readValue(requestDtoJson, PetSitterRequestDto.class);

            // 사용자 ID 설정
            requestDto.setUserId(userPrincipal.getUserId());

            log.info("펫시터 신청 요청: userId={}, age={}, houseType={}",
                    userPrincipal.getUserId(), requestDto.getAge(), requestDto.getHouseType());

            // 펫시터 신청 처리
            PetSitterResponseDto result = petSitterService.applyForPetSitter(requestDto, image);

            return ResponseEntity.ok(
                    new CustomResponse("펫시터 신청이 완료되었습니다. 관리자 승인 후 활동이 가능합니다.", result));

        } catch (Exception e) {
            log.error("펫시터 신청 중 오류 발생: userId={}, error={}", userPrincipal.getUserId(), e.getMessage(), e);

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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new CustomResponse("로그인이 필요합니다.", null));
        }

        try {
            boolean exists = petSitterService.existsById(userPrincipal.getUserId());

            if (!exists) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new CustomResponse("펫시터 정보가 없습니다.", null));
            }

            PetSitterResponseDto result = petSitterService.getPetSitterStatus(userPrincipal.getUserId());
            return ResponseEntity.ok(new CustomResponse("조회 성공", result));

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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new CustomResponse("로그인이 필요합니다.", null));
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