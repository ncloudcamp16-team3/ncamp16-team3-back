package tf.tailfriend.reserve.controller;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tf.tailfriend.facility.entity.dto.forReserve.FacilityCardResponseDto;
import tf.tailfriend.facility.service.FacilityService;
import tf.tailfriend.reserve.dto.RequestForFacility.FacilityList;
import tf.tailfriend.reserve.dto.RequestForFacility.ReviewInsertRequestDto;
import tf.tailfriend.reserve.service.ReserveService;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
@RestController
@RequestMapping("/api/reserve")
public class ReserveController {

    private final FacilityService facilityService;
    private final ReserveService reserveService;

    public ReserveController(FacilityService facilityService, ReserveService reserveService) {
        this.facilityService = facilityService;
        this.reserveService = reserveService;
    }

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
}