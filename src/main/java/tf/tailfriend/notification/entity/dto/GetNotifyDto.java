package tf.tailfriend.notification.entity.dto;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tf.tailfriend.notification.entity.Notification;
import tf.tailfriend.notification.entity.NotificationType;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetNotifyDto {

    private Integer id;
    private Integer userId;
    private Integer notificationTypeId;
    private String content;
    private Boolean readStatus;
    private String createdAt;
    private String title;
    private String body;

}
