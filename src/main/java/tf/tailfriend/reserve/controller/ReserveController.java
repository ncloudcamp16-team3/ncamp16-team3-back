package tf.tailfriend.reserve.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import tf.tailfriend.board.dto.BoardRequestDto;
import tf.tailfriend.board.entity.Board;
import tf.tailfriend.board.exception.GetPostException;
import tf.tailfriend.facility.dto.FacilityDetailDto;
import tf.tailfriend.facility.entity.Facility;
import tf.tailfriend.facility.entity.dto.forReserve.FacilityCardResponseDto;
import tf.tailfriend.facility.entity.dto.forReserve.FacilityReviewResponseDto;
import tf.tailfriend.facility.service.FacilityService;
import tf.tailfriend.global.config.UserPrincipal;
import tf.tailfriend.global.exception.CustomException;
import tf.tailfriend.global.response.CustomResponse;
import tf.tailfriend.global.service.RedisService;
import tf.tailfriend.global.service.StorageServiceException;
import tf.tailfriend.reserve.dto.RequestForFacility.FacilityList;
import tf.tailfriend.reserve.dto.RequestForFacility.ReviewInsertRequestDto;
import tf.tailfriend.reserve.dto.ReserveDetailResponseDto;
import tf.tailfriend.reserve.dto.ReserveListResponseDto;
import tf.tailfriend.reserve.dto.ReserveRequestDto;
import tf.tailfriend.reserve.dto.ReviewPageRenderingRequestDto;
import tf.tailfriend.reserve.entity.Reserve;
import tf.tailfriend.reserve.service.ReserveService;
import tf.tailfriend.user.exception.UnauthorizedException;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reserve")
public class ReserveController {

    private final FacilityService facilityService;
    private final ReserveService reserveService;
    private final RedisService redisService;


    @GetMapping("/facility/lists")
    public Slice<FacilityCardResponseDto> getFacilityList(
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam("category") String category,
            @RequestParam("sortBy") String sortBy,
            @RequestParam("day") String day,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        log.info("latitude: {}, longitude: {}, category: {}, sortBy: {}, day: {}, page: {}, size: {}", latitude, longitude, category, sortBy, day, page, size);
        String formattedSortBy = switch (sortBy) {
            case "distance" -> "distance";
            default -> "starPoint";
        };
        FacilityList requestDto = FacilityList.builder()
                .day(day)
                .userLatitude(latitude)
                .userLongitude(longitude)
                .category(category)
                .sortBy(formattedSortBy)
                .page(page)
                .size(size)
                .build();
        log.info("requestDto: {}", requestDto);

        return facilityService.getFacilityCardsForReserve(requestDto);
    }

    @PostMapping("/facility/review")
    public ResponseEntity<String> insertReview(
            @RequestPart("reviewData") ReviewInsertRequestDto requestDto,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("requestDto: {}, file: {}", requestDto, file);

        try {
            facilityService.insertReview(requestDto, userPrincipal.getUserId(), file);
            log.info("리뷰 등록 완료");
            return ResponseEntity.ok("리뷰가 성공적으로 등록되었습니다.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("리소스를 찾을 수 없습니다: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("리뷰 등록 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
  
    @PostMapping("/temp")
    public ResponseEntity<?> saveTempReserve(@RequestBody ReserveRequestDto dto) {
        String reserveKey = "reserve:temp:" + UUID.randomUUID();
        redisService.saveTempReserve(reserveKey, dto);
        return ResponseEntity.ok(Map.of("reserveId", reserveKey)); // 프론트에서 merchantPayKey로 사용
    }


    @GetMapping("/payment/naver/return")
    public void handleNaverPayReturn(
            @RequestParam String merchantPayKey,
            @RequestParam String resultCode,
            HttpServletResponse response
    ) throws IOException {
        if ("Success".equalsIgnoreCase(resultCode)) {
            Reserve saved = reserveService.saveReserveAfterPayment(merchantPayKey);

            String encodedName = URLEncoder.encode(saved.getFacility().getName(), StandardCharsets.UTF_8);

            String query = UriComponentsBuilder.fromPath("/reserve/success")
                    .queryParam("id", saved.getId())
                    .queryParam("name", encodedName)
                    .queryParam("amount", saved.getAmount())
                    .queryParam("start", saved.getEntryTime())
                    .queryParam("end", saved.getExitTime())
                    .build(false) // 인코딩 하지 않도록 설정
                    .toUriString();

            response.sendRedirect("https://tailfriends.kro.kr" + query);
            return;
        }

        response.sendRedirect("https://tailfriends.kro.kr/reserve/fail");
    }

    @GetMapping("/my")
    public List<ReserveListResponseDto> getMyReserveList(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return reserveService.getReserveListByUser(userPrincipal.getUserId());
    }

    @GetMapping("/facility/{id}/review")
    public FacilityReviewResponseDto getFacilityForReview(@PathVariable("id") String id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("id: {}", id);
        Integer parsedId = Integer.parseInt(id);
        ReviewPageRenderingRequestDto requestDto = ReviewPageRenderingRequestDto.builder()
                .userId(userPrincipal.getUserId())
                .reserveId(parsedId)
                .build();
        return reserveService.getReserveForReview(requestDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReserveDetailResponseDto> getReserveDetail(@PathVariable Integer id) {
        ReserveDetailResponseDto dto = reserveService.getReserveDetail(id);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("")
    public ResponseEntity<?> cancelReserve(@RequestParam("id") Integer id,
                                           @AuthenticationPrincipal UserPrincipal userPrincipal) {

        reserveService.cancelReserve(id, userPrincipal.getUserId());

        return ResponseEntity.status(HttpStatus.OK)
                .body(new CustomResponse("예약 취소에 성공하였습니다", null));
    }

    @PutMapping("/facility/review/{id}")
    public ResponseEntity<?> updateReview(@PathVariable Integer id,
                                          @AuthenticationPrincipal UserPrincipal user,
                                          @RequestParam("comment") String comment,
                                          @RequestParam("starPoint") int starPoint,
                                          @RequestPart(value = "image", required = false) MultipartFile image) {
        try {
            FacilityDetailDto detailDto = facilityService.updateReview(id, user.getUserId(), comment, starPoint, image);
            return ResponseEntity.ok().body(detailDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("리뷰를 찾을 수 없습니다: " + e.getMessage());
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("리뷰를 수정할 권한이 없습니다: " + e.getMessage());
        } catch (StorageServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("리뷰 이미지 재 업로드 중 오류가 발생하였습니다: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("리뷰 수정 중 알 수 없는 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @DeleteMapping("/facility/review/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Integer id,
                                          @AuthenticationPrincipal UserPrincipal user) {
        try {
            FacilityDetailDto detailDto = facilityService.deleteReview(id, user.getUserId());
            return ResponseEntity.ok().body(detailDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("리뷰를 찾을 수 없습니다: " + e.getMessage());
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("리뷰를 삭제할 권한이 없습니다: " + e.getMessage());
        } catch (StorageServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("이미지 삭제 중 오류가 발생하였습니다: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("리뷰 삭제 중 알 수 없는 오류가 발생했습니다: " + e.getMessage());
        }
    }

}