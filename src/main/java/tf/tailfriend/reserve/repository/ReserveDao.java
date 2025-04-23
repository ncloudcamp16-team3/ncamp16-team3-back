package tf.tailfriend.reserve.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.reserve.entity.Reserve;

public interface ReserveDao extends JpaRepository<Reserve, Integer> {



}
