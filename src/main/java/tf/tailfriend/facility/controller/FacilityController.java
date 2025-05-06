package tf.tailfriend.facility.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tf.tailfriend.facility.service.FacilityService;
import tf.tailfriend.global.response.CustomResponse;

import java.util.Map;

@RestController
@RequestMapping("/api/facility")
@RequiredArgsConstructor
public class FacilityController {

    private final FacilityService facilityService;

    @GetMapping("/{id}/detail")
    public ResponseEntity<CustomResponse> getFacilityDetailWithReviews(@PathVariable Integer id) {
        Map<String, Object> response = facilityService.getFacilityDetailWithReviews(id);
        return ResponseEntity.ok(new CustomResponse("시설 상세 정보를 조회했습니다.", response));
    }

}
