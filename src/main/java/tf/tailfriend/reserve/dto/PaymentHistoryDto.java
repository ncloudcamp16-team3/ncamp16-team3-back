package tf.tailfriend.reserve.dto;

import com.fasterxml.jackson.core.JsonToken;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentHistoryDto {
    private Integer id;
    private String name; // 시설 이름
    private String imageUrl; // 썸네일
    private LocalDateTime createdAt; // 결제 일시
    private Integer reserveId;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private Integer price;
    private Integer reviewId;

}
