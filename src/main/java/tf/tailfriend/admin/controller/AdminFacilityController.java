package tf.tailfriend.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tf.tailfriend.facility.dto.FacilityResponseDto;
import tf.tailfriend.facility.entity.FacilityType;
import tf.tailfriend.facility.service.FacilityService;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminFacilityController {

    private final FacilityService facilityService;

    @GetMapping("/facility/list")
    public ResponseEntity<?> facilityList(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false) Integer facilityTypeId,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false, defaultValue = "all") String searchField
    ) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").descending());
        Page<FacilityResponseDto> facilities;

        // 검색 조건에 따른 서비스 메소드 호출
        if (searchTerm != null && !searchTerm.isEmpty()) {
            facilities = facilityService.searchFacilities(
                    facilityTypeId, searchTerm, searchField, pageRequest);
        } else if (facilityTypeId != null) {
            facilities = facilityService.findByFacilityType(facilityTypeId, pageRequest);
        } else {
            facilities = facilityService.findAll(pageRequest);
        }

        return ResponseEntity.ok(facilities);
    }

    @GetMapping("/facility/list/{id}")
    public ResponseEntity<?> getFacilityDetail(@PathVariable Integer id) {
        FacilityResponseDto facility = facilityService.getFacilityById(id);
        return ResponseEntity.ok(facility);
    }
}
