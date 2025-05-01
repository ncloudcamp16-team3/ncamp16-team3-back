package tf.tailfriend.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.notification.entity.Notification;

import java.util.List;
import java.util.Optional;

public interface NotificationDao extends JpaRepository<Notification, Integer> {


    boolean existsByMessageId(String messageId);

    List<Notification> findByUserId(Integer userId);

}
