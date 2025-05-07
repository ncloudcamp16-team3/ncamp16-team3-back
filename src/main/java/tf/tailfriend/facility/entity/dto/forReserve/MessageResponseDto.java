package tf.tailfriend.facility.entity.dto.forReserve;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MessageResponseDto {
    private String message;
    private List<String> buttons;
    private List<String> redirectUrls;
    private List<String> buttonStyles;
}
