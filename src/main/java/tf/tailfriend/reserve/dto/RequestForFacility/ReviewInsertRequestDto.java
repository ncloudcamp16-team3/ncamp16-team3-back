package tf.tailfriend.reserve.dto.RequestForFacility;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class ReviewInsertRequestDto {
    private Integer reserveId;
    private Integer facilityId;
    private String comment;
    private Integer starPoint;
}
