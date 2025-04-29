package tf.tailfriend.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.notification.entity.Notification;

import java.util.List;

public interface NotificationDao extends JpaRepository<Notification, Integer> {

}
