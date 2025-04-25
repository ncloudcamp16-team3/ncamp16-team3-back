package tf.tailfriend.facility.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.facility.entity.FacilityTimetable;

import java.util.List;

public interface FacilityTimetableDao extends JpaRepository<FacilityTimetable, Integer> {

    List<FacilityTimetable> findByFacilityId(Integer id);
}
