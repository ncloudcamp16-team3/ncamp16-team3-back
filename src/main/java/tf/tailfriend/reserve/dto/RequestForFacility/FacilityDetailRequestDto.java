package tf.tailfriend.reserve.dto.RequestForFacility;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FacilityDetailRequestDto {
    private Integer id;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 3;
}
