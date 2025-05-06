package tf.tailfriend.reserve.dto.RequestForFacility;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FacilityReviewRequestDto {
    private Integer facilityId;
    private Integer page;
    private Integer size;
}
