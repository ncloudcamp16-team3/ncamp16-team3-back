package tf.tailfriend.reserve.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Builder
public class ReserveListRequestDto {

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime localDateTime;

    private double latitude;

    private double longitude;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 10;
}
