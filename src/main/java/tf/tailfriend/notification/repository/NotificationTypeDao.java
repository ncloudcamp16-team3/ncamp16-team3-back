package tf.tailfriend.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.notification.entity.Notification;
import tf.tailfriend.notification.entity.NotificationType;

import java.util.List;

public interface NotificationTypeDao extends JpaRepository<NotificationType, Integer> {

}
