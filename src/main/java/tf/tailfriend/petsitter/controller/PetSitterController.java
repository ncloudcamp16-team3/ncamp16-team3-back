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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/petsitter")
@RequiredArgsConstructor
@Slf4j
public class PetSitterController {

    private final PetSitterService petSitterService;
    private final ObjectMapper objectMapper;


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
            log.info("펫시터 신청 요청 데이터: {}", requestDtoJson);
            PetSitterRequestDto requestDto = objectMapper.readValue(requestDtoJson, PetSitterRequestDto.class);

            // 사용자 ID 설정
            requestDto.setUserId(userPrincipal.getUserId());

            log.info("펫시터 신청 처리 시작: userId={}, age={}, houseType={}",
                    userPrincipal.getUserId(), requestDto.getAge(), requestDto.getHouseType());

            if (requestDto.getPetTypesFormatted() != null) {
                log.info("선택된 반려동물 타입: {}", requestDto.getPetTypesFormatted());
            }
            if (requestDto.getPetTypeIds() != null && !requestDto.getPetTypeIds().isEmpty()) {
                log.info("선택된 반려동물 타입 ID: {}", requestDto.getPetTypeIds());
            }

            // 펫시터 신청 처리
            PetSitterResponseDto result = petSitterService.applyForPetSitter(requestDto, image);
            log.info("펫시터 신청 처리 완료: userId={}, status={}",
                    userPrincipal.getUserId(), result.getStatus());

            return ResponseEntity.ok(
                    new CustomResponse("펫시터 신청이 완료되었습니다. 관리자 승인 후 활동이 가능합니다.", result));

        } catch (Exception e) {
            log.error("펫시터 신청 중 오류 발생: userId={}, error={}", userPrincipal.getUserId(), e.getMessage(), e);

            // 특정 오류 타입에 따른 응답 처리
            if (e instanceof IllegalArgumentException) {
                return ResponseEntity.badRequest()
                        .body(new CustomResponse(e.getMessage(), null));
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CustomResponse("펫시터 신청 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }

    //사용자의 펫시터 신청 상태를 조회하는 API
    @GetMapping("/status")
    public ResponseEntity<?> getPetSitterStatus(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new CustomResponse("로그인이 필요합니다.", null));
        }

        try {
            log.info("펫시터 상태 조회 요청: userId={}", userPrincipal.getUserId());
            boolean exists = petSitterService.existsById(userPrincipal.getUserId());

            if (!exists) {
                log.info("펫시터 정보 없음: userId={}", userPrincipal.getUserId());
                return ResponseEntity.ok(new CustomResponse("펫시터 정보가 없습니다.", null));
            }

            PetSitterResponseDto result = petSitterService.getPetSitterStatus(userPrincipal.getUserId());
            log.info("펫시터 상태 조회 완료: userId={}, status={}", userPrincipal.getUserId(), result.getStatus());

            return ResponseEntity.ok(new CustomResponse("조회 성공", result));

        } catch (Exception e) {
            log.error("펫시터 상태 조회 오류: userId={}", userPrincipal.getUserId(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CustomResponse("펫시터 상태 조회 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }

    // 사용자가 펫시터를 그만두는 API
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

    // 조건에 맞는 승인된 펫시터 목록을 조회하는 API
    @GetMapping("/approved")
    public ResponseEntity<?> getApprovedPetSitters(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) String age,
            @RequestParam(required = false) Boolean petOwnership,
            @RequestParam(required = false) Boolean sitterExp,
            @RequestParam(required = false) String houseType,
            Pageable pageable) {

        try {
            log.info("승인된 펫시터 목록 조회: age={}, petOwnership={}, sitterExp={}, houseType={}",
                    age, petOwnership, sitterExp, houseType);

            // 페이지 사이즈 기본값 설정
            if (pageable.getPageSize() > 50) {
                pageable = PageRequest.of(pageable.getPageNumber(), 50, pageable.getSort());
            }

            // 현재 사용자 ID 가져오기
            Integer currentUserId = userPrincipal != null ? userPrincipal.getUserId() : null;

            // 승인된 펫시터 중 조건에 맞는 목록 조회 (현재 사용자 제외)
            Page<PetSitterResponseDto> results = petSitterService.findApprovedPetSittersWithCriteria(
                    age, petOwnership, sitterExp, houseType, pageable, currentUserId);

            log.info("승인된 펫시터 목록 조회 완료: totalElements={}", results.getTotalElements());

            return ResponseEntity.ok(new CustomResponse("조회 성공", results));

        } catch (Exception e) {
            log.error("승인된 펫시터 목록 조회 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CustomResponse("펫시터 목록 조회 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }

    // 펫시터 목록을 조회하는 API
    @GetMapping("/list")
    public ResponseEntity<?> getPetSitterList(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String age,
            @RequestParam(required = false) Boolean petOwnership,
            @RequestParam(required = false) Boolean sitterExp,
            @RequestParam(required = false) String houseType,
            Pageable pageable) {

        try {
            log.info("펫시터 목록 조회: status={}", status);

            // 페이지 사이즈 기본값 설정
            if (pageable.getPageSize() > 50) {
                pageable = PageRequest.of(pageable.getPageNumber(), 50, pageable.getSort());
            }

            // 현재 사용자 ID 가져오기
            Integer currentUserId = userPrincipal != null ? userPrincipal.getUserId() : null;

            Page<PetSitterResponseDto> results;

            // "APPROVE" 상태인 경우에만 현재 사용자 제외 로직 적용
            if ("APPROVE".equals(status)) {
                results = petSitterService.findAllApprovedPetSitters(
                        age, petOwnership, sitterExp, houseType, pageable, currentUserId);
            } else if ("NONE".equals(status)) {
                results = petSitterService.findNonePetSitter(pageable);
            } else {
                results = petSitterService.findAll(pageable);
            }

            log.info("펫시터 목록 조회 완료: totalElements={}", results.getTotalElements());

            return ResponseEntity.ok(new CustomResponse("조회 성공", results));

        } catch (Exception e) {
            log.error("펫시터 목록 조회 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CustomResponse("펫시터 목록 조회 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }

    //특정 펫시터의 상세 정보를 조회하는 API
    @GetMapping("/{id}")
    public ResponseEntity<?> getPetSitterDetail(@PathVariable Integer id) {
        try {
            log.info("펫시터 상세 정보 조회: id={}", id);

            PetSitterResponseDto result = petSitterService.findById(id);

            // 사용자 정보 추가 (닉네임 등)
            if (result != null) {
                log.info("펫시터 상세 정보 조회 성공: id={}, nickname={}", id, result.getNickname());
                return ResponseEntity.ok(new CustomResponse("조회 성공", result));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new CustomResponse("펫시터 정보를 찾을 수 없습니다.", null));
            }
        } catch (IllegalArgumentException e) {
            log.warn("펫시터 상세 정보 조회 실패 - 유효하지 않은 ID: id={}, message={}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new CustomResponse(e.getMessage(), null));
        } catch (Exception e) {
            log.error("펫시터 상세 정보 조회 오류: id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CustomResponse("펫시터 정보 조회 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }
}