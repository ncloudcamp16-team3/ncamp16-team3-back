package tf.tailfriend.reserve.dto.RequestForFacility;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewInsertRequestDto {
    private Integer id;
    private String comment;
    private Integer starPoint;
}
