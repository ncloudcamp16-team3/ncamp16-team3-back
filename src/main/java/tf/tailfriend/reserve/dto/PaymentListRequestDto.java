package tf.tailfriend.reserve.dto;

import lombok.*;

@Getter
@Builder
public class PaymentListRequestDto {

    private Integer userId;

    private DateTimeRange datetimeRange;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 10;

}
