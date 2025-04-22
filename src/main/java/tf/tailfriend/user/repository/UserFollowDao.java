package tf.tailfriend.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.user.entity.User;
import tf.tailfriend.user.entity.UserFollow;

import java.util.Optional;

public interface UserFollowDao extends JpaRepository<UserFollow, Integer> {
    Optional<UserFollow> findByFollowerIdAndFollowedId(Integer followerId, Integer followedId);
    boolean existsByFollowerIdAndFollowedId(Integer followerId, Integer followedId);

}
