package tf.tailfriend.notification.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto implements Serializable {
    private static final long serialVersionUID = 1L;  // UID 추가 (버전 관리)

    private Integer userId;
    private Integer notifyTypeId;
    private String content;
    private String messageId;
    private String fcmToken;

    // 채팅 알림 전용 필드
    private String senderId;
    private String message;

    // 추가된 필드
    private boolean mobile;
    private boolean dev;
}
