package tf.tailfriend.reserve.controller;

import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tf.tailfriend.facility.entity.dto.forReserve.FacilityCardResponseDto;
import tf.tailfriend.facility.entity.dto.forReserve.FacilityDetailResponseDto;
import tf.tailfriend.facility.service.FacilityService;
import tf.tailfriend.reserve.dto.RequestForFacility.FacilityDetailRequestDto;
import tf.tailfriend.reserve.dto.RequestForFacility.FacilityListRequestDto;
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

    @GetMapping("/facility/lists")
    public Slice<FacilityCardResponseDto> getFacilityList(
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam("category") String category,
            @RequestParam("sortBy") String sortBy,
            @RequestParam("day") String day,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        String formattedSortBy = switch (sortBy) {
            case "distance" -> "distance";
            default -> "starPoint";
        };
        FacilityListRequestDto requestDto = FacilityListRequestDto.builder()
                .day(day)
                .userLatitude(latitude)
                .userLongitude(longitude)
                .category(category)
                .sortBy(formattedSortBy)
                .page(page)
                .size(size)
                .build();

        return facilityService.getFacilityCardsForReserve(requestDto);
    }

    @GetMapping("facility/{facilityId}")
    public ResponseEntity<FacilityDetailResponseDto> getFacility(
            @PathVariable Integer facilityId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "3") Integer size) {

        FacilityDetailRequestDto requestDto = FacilityDetailRequestDto.builder()
                .id(facilityId)
                .page(page)
                .size(size)
                .build();
        FacilityDetailResponseDto facilityDetail = facilityService.getFacilityWithReviews(requestDto);
        return ResponseEntity.ok(facilityDetail);
    }
}