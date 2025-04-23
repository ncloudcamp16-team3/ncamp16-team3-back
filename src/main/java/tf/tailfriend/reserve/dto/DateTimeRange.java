package tf.tailfriend.reserve.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 시작 시간과 종료 시간을 담는 불변 객체입니다.
 */
@Getter
public class DateTimeRange {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime openDateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime closeDateTime;

    public DateTimeRange(LocalDateTime openDateTime, LocalDateTime closeDateTime) {
        this.openDateTime = openDateTime;
        this.closeDateTime = closeDateTime;
    }

}