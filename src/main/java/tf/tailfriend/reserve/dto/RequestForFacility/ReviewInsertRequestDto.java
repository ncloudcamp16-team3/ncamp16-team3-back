package tf.tailfriend.reserve.dto.RequestForFacility;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewInsertRequestDto {
    private Integer userId;
    private Integer facilityId;
    private String comment;
    private Integer starPoint;
}
