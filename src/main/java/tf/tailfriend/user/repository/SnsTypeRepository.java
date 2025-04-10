package tf.tailfriend.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.user.entity.SnsTypes;

public interface SnsTypeRepository extends JpaRepository<SnsTypes, Integer> {
}
