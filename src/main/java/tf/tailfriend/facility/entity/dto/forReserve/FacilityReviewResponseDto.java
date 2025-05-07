package tf.tailfriend.facility.entity.dto.forReserve;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FacilityReviewResponseDto {
    private Integer id;
    private String name;
    private String thumbnail;
    private String errorMsg;
}
