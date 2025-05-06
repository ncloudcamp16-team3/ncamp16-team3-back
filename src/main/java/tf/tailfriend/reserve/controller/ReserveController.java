package tf.tailfriend.reserve.controller;

<<<<<<< HEAD
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
=======
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
>>>>>>> aa8c1c467fbd3053c089c497665a3efb725bd691
import tf.tailfriend.facility.entity.dto.forReserve.FacilityCardResponseDto;
import tf.tailfriend.facility.service.FacilityService;
import tf.tailfriend.global.service.RedisService;
import tf.tailfriend.reserve.dto.RequestForFacility.FacilityList;
<<<<<<< HEAD
import tf.tailfriend.reserve.dto.RequestForFacility.ReviewInsertRequestDto;
import tf.tailfriend.reserve.service.ReserveService;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
=======
import tf.tailfriend.reserve.dto.ReserveRequestDto;
import tf.tailfriend.reserve.entity.Reserve;
import tf.tailfriend.reserve.service.ReserveService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
>>>>>>> aa8c1c467fbd3053c089c497665a3efb725bd691

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

<<<<<<< HEAD
    @PutMapping("/facility/{id}/review")
    public ResponseEntity<String> insertReview(
            @PathVariable("id") Integer id,
            @RequestParam("comment") String comment,
            @RequestParam("starPoint") Integer starPoint,
            @RequestParam("image") File image) {
        log.info("id: {}, comment: {}, starPoint: {}", id, comment, starPoint);
        ReviewInsertRequestDto requestDto = ReviewInsertRequestDto.builder()
                .id(id)
                .comment(comment)
                .starPoint(starPoint)
                .build();
        try {
            facilityService.insertReview(requestDto, image);
            return ResponseEntity.ok("리뷰가 성공적으로 등록되었습니다.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("리소스를 찾을 수 없습니다: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("리뷰 등록 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
=======
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
                    .queryParam("name", encodedName)
                    .queryParam("amount", saved.getAmount())
                    .queryParam("start", saved.getEntryTime())
                    .queryParam("end", saved.getExitTime())
                    .build(false) // 인코딩 하지 않도록 설정
                    .toUriString();

            response.sendRedirect("http://localhost:5173" + query);
            return;
        }

        response.sendRedirect("http://localhost:5173/reserve/fail");
    }



>>>>>>> aa8c1c467fbd3053c089c497665a3efb725bd691
}