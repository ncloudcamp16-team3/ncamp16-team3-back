package tf.tailfriend.chat.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatRoomListResponseDto {
    private String uniqueId;
    private String nickname;
    private String profileUrl;
}
