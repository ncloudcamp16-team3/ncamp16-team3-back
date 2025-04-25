package tf.tailfriend.schedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.schedule.entity.Event;
import tf.tailfriend.schedule.entity.Schedule;

public interface EventDao extends JpaRepository<Event, Integer> {
}
