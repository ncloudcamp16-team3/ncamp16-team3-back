package tf.tailfriend.notification.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;




@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {

    private Integer userId;
    private Integer notifyTypeId;
    private String content;
    private String messageId;
    private String fcmToken;

    // 채팅 알림 전용 필드
    private String senderId;
    private String message;

    // 추가된 필드
    private Boolean isMobile;
    private Boolean isDev;
}
