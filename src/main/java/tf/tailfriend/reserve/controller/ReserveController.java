package tf.tailfriend.reserve.controller;

import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tf.tailfriend.facility.entity.Facility;
import tf.tailfriend.facility.entity.dto.forReserve.FacilityCardResponseDto;
import tf.tailfriend.facility.entity.dto.forReserve.FacilityDetailResponseDto;
import tf.tailfriend.facility.entity.dto.forReserve.FacilityReviewResponseDto;
import tf.tailfriend.facility.service.FacilityService;
import tf.tailfriend.reserve.dto.RequestForFacility.FacilityDetailRequestDto;
import tf.tailfriend.reserve.dto.RequestForFacility.FacilityListRequestDto;
import tf.tailfriend.reserve.dto.RequestForFacility.FacilityReviewRequestDto;
import tf.tailfriend.reserve.service.ReserveService;
import lombok.extern.slf4j.Slf4j;

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

    @GetMapping("/facility/list")
    public Slice<FacilityCardResponseDto> getFacilityList(
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam("category") String category,
            @RequestParam("sortBy") String sortBy,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        String formattedSortBy = switch (sortBy) {
            case "distance" -> "distance";
            default -> "starPoint";
        };
        FacilityListRequestDto requestDto = FacilityListRequestDto.builder()
                .userLatitude(latitude)
                .userLongitude(longitude)
                .category(category)
                .sortBy(formattedSortBy)
                .page(page)
                .size(size)
                .build();

        return facilityService.getFacilityCardsForReserve(requestDto);
    }

    @GetMapping("/facility/detail/{id}")
    public ResponseEntity<FacilityDetailResponseDto> getFacility(
            @PathVariable Integer id) {

        FacilityDetailRequestDto requestDto = FacilityDetailRequestDto.builder()
                .id(id)
                .build();
        FacilityDetailResponseDto facilityDetail = facilityService.getFacility(requestDto);
        return ResponseEntity.ok(facilityDetail);
    }

    @GetMapping("/facility/detail/{id}/review/")
    public Slice<FacilityReviewResponseDto> getFacilityReview(@PathVariable("id") Integer id,
                                                              @RequestParam("page") Integer page,
                                                              @RequestParam("size") Integer size) {
        FacilityReviewRequestDto requestDto = FacilityReviewRequestDto.builder()
                .facilityId(id)
                .page(page)
                .size(size)
                .build();
        return facilityService.getFacilityReview(requestDto);
    }
}