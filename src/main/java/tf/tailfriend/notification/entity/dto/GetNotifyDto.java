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
    private LocalDateTime createdAt;
    private String title;
    private String body;

    public GetNotifyDto(Notification notification) {
        this.id = notification.getId();
        this.userId = notification.getUser().getId();
        this.notificationTypeId = notification.getNotificationType().getId();
        this.content = notification.getContent();
        this.readStatus = notification.getReadStatus();
        this.createdAt = notification.getCreatedAt();

    }
}
