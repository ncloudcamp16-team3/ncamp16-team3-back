package tf.tailfriend.reserve.dto;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import tf.tailfriend.global.service.DateTimeRange;
import tf.tailfriend.reserve.entity.Payment;

import java.time.LocalDateTime;

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
