package tf.tailfriend.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.notification.entity.UserFcm;
import tf.tailfriend.notification.entity.dto.UserFcmDto;

import java.util.List;
import java.util.Optional;

public interface UserFcmDao  extends JpaRepository<UserFcm, Integer> {

    Optional<UserFcm> findByUserId(Integer userId);

    List<UserFcm> findUserFcmByUserId(Integer userId);

    List<UserFcm> findAllByUserId(Integer userId);

    Optional<UserFcm> findByUserIdAndMobileAndDev(Integer userId, boolean isMobile, boolean isDev);
}
