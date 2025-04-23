package tf.tailfriend.reserve.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentInfoResponseDto {

    private Integer id;

    private String name;

    private String createdAt;

    private Integer price;

}