package tf.tailfriend.reserve.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tf.tailfriend.reserve.entity.Reserve;
import tf.tailfriend.schedule.entity.Schedule;

import java.time.LocalDateTime;
import java.util.List;

public interface ReserveDao extends JpaRepository<Reserve, Integer> {

    @EntityGraph(attributePaths = {
            "facility",
            "facility.facilityType",
            "facility.photos",
            "facility.photos.file"
    })
    List<Reserve> findByUserId(Integer UserId);

    List<Reserve> findByEntryTimeBetween(LocalDateTime now, LocalDateTime tenMinutesLater);


    @Query("SELECT r FROM Reserve r WHERE r.user.id = :userId")
    List<Reserve> findAllByUserId(@Param("userId") Integer userId);

    List<Reserve> findByUserIdOrderByIdDesc(Integer UserId);
}
