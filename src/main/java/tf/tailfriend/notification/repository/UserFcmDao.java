package tf.tailfriend.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.notification.entity.UserFcm;
import tf.tailfriend.notification.entity.dto.UserFcmDto;

import java.util.Optional;

public interface UserFcmDao  extends JpaRepository<UserFcm, Integer> {

    Optional<UserFcm> findByUserId(Integer userId);
}
