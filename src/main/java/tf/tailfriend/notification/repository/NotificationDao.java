package tf.tailfriend.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.notification.entity.Notification;
import tf.tailfriend.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface NotificationDao extends JpaRepository<Notification, Integer> {


    boolean existsByMessageId(String messageId);

    List<Notification> findByUserId(Integer userId);

    List<Notification> findByUserIdOrderByCreatedAtDesc(Integer userId);

    void deleteByUserId(Integer userId);

    Notification findFirstByUserAndNotificationTypeIdAndContent(User user, Integer notificationTypeId, String content);

    boolean existsByUserIdAndReadStatusFalse(Integer userId);

    boolean existsByUserIdAndReadStatusFalseAndNotificationTypeIdNot(Integer userId, int i);
}
