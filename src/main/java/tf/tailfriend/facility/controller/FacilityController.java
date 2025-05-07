package tf.tailfriend.facility.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tf.tailfriend.facility.dto.FacilityDetailDto;
import tf.tailfriend.facility.service.FacilityService;

import java.util.Map;

@RestController
@RequestMapping("/api/facility")
@RequiredArgsConstructor
public class FacilityController {

    private final FacilityService facilityService;

    @GetMapping("/{id}/detail")
    public ResponseEntity<?> getFacilityDetailWithReviews(@PathVariable Integer id) {
        FacilityDetailDto detailDto = facilityService.getFacilityDetailWithReviews(id);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("data", detailDto));
    }

}
