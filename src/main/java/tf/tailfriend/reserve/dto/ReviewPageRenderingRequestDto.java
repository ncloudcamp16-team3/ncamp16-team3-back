package tf.tailfriend.reserve.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewPageRenderingRequestDto {
    private Integer userId;
    private Integer reserveId;
}
