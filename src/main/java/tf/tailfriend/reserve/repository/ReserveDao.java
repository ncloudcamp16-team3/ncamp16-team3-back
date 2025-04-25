package tf.tailfriend.reserve.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.reserve.entity.Reserve;
import tf.tailfriend.schedule.entity.Schedule;

import java.util.List;

public interface ReserveDao extends JpaRepository<Reserve, Integer> {
    List<Reserve> findByUserId(Integer UserId);

}
