package tf.tailfriend.facility.entity.dto.ResponseForReserve;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeRangeForWeek {
    private String day;           // 예: "월요일"
    private String openTimeRange; // 예: "09:00 - 18:00", null 가능
}
