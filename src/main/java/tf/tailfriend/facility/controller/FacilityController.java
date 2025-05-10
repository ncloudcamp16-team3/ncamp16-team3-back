package tf.tailfriend.facility.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tf.tailfriend.facility.dto.FacilityDetailDto;
import tf.tailfriend.facility.service.FacilityService;
import tf.tailfriend.global.config.UserPrincipal;
import tf.tailfriend.global.service.StorageServiceException;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.Map;

@RestController
@RequestMapping("/api/facility")
@RequiredArgsConstructor
public class FacilityController {

    private final FacilityService facilityService;

    @GetMapping("/{id}/detail")
    public ResponseEntity<?> getFacilityDetailWithReviews(@PathVariable Integer id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Integer userId = userPrincipal != null ? userPrincipal.getUserId() : null;
        FacilityDetailDto detailDto = facilityService.getFacilityDetailWithReviews(id, userId);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("data", detailDto));
    }

}
