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

}
