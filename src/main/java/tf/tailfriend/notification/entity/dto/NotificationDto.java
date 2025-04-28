package tf.tailfriend.notification.entity.dto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import tf.tailfriend.notification.entity.NotificationType;
import tf.tailfriend.user.entity.User;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {


    private Integer userId;
    private Integer notifyTypeId;
    private String content;

}
