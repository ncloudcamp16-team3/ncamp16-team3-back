package tf.tailfriend.schedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.schedule.entity.Schedule;
import tf.tailfriend.schedule.entity.dto.ScheduleDTO.*;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface ScheduleDao extends JpaRepository<Schedule, Integer>  {

    List<Schedule> findByUserId(Integer UserId);
}
