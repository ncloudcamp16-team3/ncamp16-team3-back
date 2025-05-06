package tf.tailfriend.reserve.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReserveRequestDto {
    private Integer userId;
    private Integer facilityId;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private Integer amount;
}
