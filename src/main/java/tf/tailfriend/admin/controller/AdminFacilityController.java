package tf.tailfriend.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tf.tailfriend.facility.dto.FacilityRequestDto;
import tf.tailfriend.facility.dto.FacilityResponseDto;
import tf.tailfriend.facility.service.FacilityService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
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

    @GetMapping("/facility/{id}")
    public ResponseEntity<?> getFacilityDetail(@PathVariable Integer id) {
        FacilityResponseDto facility = facilityService.getFacilityById(id);
        return ResponseEntity.ok(facility);
    }

    @PostMapping("/facility/add")
    public ResponseEntity<?> addFacility(
            @RequestPart("data")FacilityRequestDto requestDto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        log.info("Facility added: {}", requestDto);
        facilityService.saveFacility(requestDto, images);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "업체가 성공적으로 등록되었습니다"));
    }

    @PostMapping("/facility/{id}/delete")
    public ResponseEntity<?> deleteFacility(@PathVariable Integer id) {
        facilityService.deleteFacilityById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "업체가 성공적으로 삭제되었습니다"));
    }
}
