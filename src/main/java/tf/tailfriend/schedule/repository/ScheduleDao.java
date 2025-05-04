package tf.tailfriend.schedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tf.tailfriend.schedule.entity.Schedule;
import tf.tailfriend.schedule.entity.dto.ScheduleDTO.*;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface ScheduleDao extends JpaRepository<Schedule, Integer>  {

    List<Schedule> findByUserId(Integer UserId);

    List<Schedule> findByStartDateBetween(LocalDateTime now, LocalDateTime tenMinutesLater);

    @Modifying
    @Query("DELETE FROM Schedule s WHERE s.user.id = :userId")
    void deleteByUserId(@Param("userId") Integer userId);
}
