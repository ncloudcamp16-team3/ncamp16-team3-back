package tf.tailfriend.facility.entity.dto.forReserve;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FacilityReviewResponseDto {
    private Integer id;
    private Integer starPoint;
    private String avatar;
    private String user;
    private String image;
    private String comment;
    private String date;

}
