package tf.tailfriend.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.user.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findBySnsAccountId(String snsAccountId);
    Optional<User> findByNickname(String nickname);
}
