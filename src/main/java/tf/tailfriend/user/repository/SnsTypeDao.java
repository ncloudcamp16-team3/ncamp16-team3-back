package tf.tailfriend.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.user.entity.SnsType;

public interface SnsTypeDao extends JpaRepository<SnsType, Integer> {
}
