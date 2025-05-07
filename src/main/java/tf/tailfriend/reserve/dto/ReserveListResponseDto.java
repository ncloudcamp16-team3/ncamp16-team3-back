package tf.tailfriend.reserve.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// ReserveListResponseDto.java
@Builder
@Getter
public class ReserveListResponseDto {
    private Integer id;
    private String name;
    private String address;
    private String type;
    private Boolean status;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private Integer amount;
    private String image;
}
