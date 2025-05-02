package tf.tailfriend.notification.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatNotificationDto {
    private Integer userId;
    private String channelId;
    private String senderId;
    private String message;
    private String type;
    private LocalDateTime createdAt;
}