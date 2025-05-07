package tf.tailfriend.admin.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tf.tailfriend.facility.dto.FacilityAddResponseDto;
import tf.tailfriend.facility.dto.FacilityRequestDto;
import tf.tailfriend.facility.dto.FacilityResponseDto;
import tf.tailfriend.facility.service.FacilityService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            @RequestPart("data") FacilityRequestDto requestDto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        log.info("Facility added: {}", requestDto);
        facilityService.saveFacility(requestDto, images);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "업체가 성공적으로 등록되었습니다"));
    }

    @PatchMapping("/facility/{id}/update")
    public ResponseEntity<?> updateFacility(
            @PathVariable Integer id,
            @RequestPart("data") FacilityRequestDto requestDto,
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages,
            @RequestParam(value = "imageIdsToKeep", required = false) String filePathToKeppJson
    ) {
        log.info("Facility update request received for id: {}", id);
        log.info("Request data: {}", requestDto);
        log.info("New images count: {}", newImages != null ? newImages.size() : 0);
        log.info("Image IDs to keep (raw): {}", filePathToKeppJson);

        List<String> filePathToKeep = new ArrayList<>();

        if (filePathToKeppJson != null && !filePathToKeppJson.isEmpty()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                filePathToKeep = objectMapper.readValue(filePathToKeppJson, new TypeReference<List<String>>() {});

                log.info("Parsed filePath to keep: {}", filePathToKeep);
            } catch (Exception e) {
                log.error("Failed to parse imageIdsToKeep JSON: {}", e.getMessage(), e);
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "이미지 ID 목록 파싱 오류: " + e.getMessage()));
            }
        } else {
            log.info("No imageIdsToKeep parameter provided, all existing images will be removed");
        }

        try {
            // ✅ 서비스 메소드 호출 전에 로그 추가
            log.info("Updating facility with id: {}, imageIdsToKeep: {}", id, filePathToKeep);
            facilityService.updateFacility(id, requestDto, newImages, filePathToKeep);
            return ResponseEntity.ok()
                    .body(Map.of("message", "업체가 성공적으로 업데이트되었습니다"));
        } catch (Exception e) {
            log.error("Error updating facility: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "업체 업데이트 중 오류 발생: " + e.getMessage()));
        }
    }

    @DeleteMapping("/facility/{id}/delete")
    public ResponseEntity<?> deleteFacility(@PathVariable Integer id) {
        facilityService.deleteFacilityById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "업체가 성공적으로 삭제되었습니다"));
    }
}
