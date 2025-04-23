package tf.tailfriend.reserve.controller;

import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tf.tailfriend.facility.dto.FacilityCardForReserve;
import tf.tailfriend.facility.service.FacilityService;
import tf.tailfriend.reserve.dto.FacilityListRequestDto;
import tf.tailfriend.reserve.dto.ListResponseDto;
import tf.tailfriend.reserve.service.ReserveService;

@RestController
@RequestMapping("/api/reserve")
public class ReserveController {

    private final FacilityService facilityService;
    private final ReserveService reserveService;

    public ReserveController(FacilityService facilityService, ReserveService reserveService) {
        this.facilityService = facilityService;
        this.reserveService = reserveService;
    }

    @GetMapping("/get")
    public Slice<FacilityCardForReserve> getFacilityList(
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam("category") String category,
            @RequestParam("SortBy") String sortBy,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        FacilityListRequestDto requestDto = FacilityListRequestDto.builder()
                .userLatitude(latitude)
                .userLongitude(longitude)
                .category(category)
                .sortBy(sortBy)
                .page(page)
                .size(size)
                .build();

        return facilityService.getFacilityCardsForReserve(requestDto);
    }

}