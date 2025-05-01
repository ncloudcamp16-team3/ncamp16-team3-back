package tf.tailfriend.facility.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.facility.entity.Facility;
import tf.tailfriend.facility.entity.FacilityTimetable;

import java.util.List;
import java.util.Optional;

public interface FacilityTimetableDao extends JpaRepository<FacilityTimetable, Integer> {

    List<FacilityTimetable> findByFacilityId(Integer id);

    Optional<FacilityTimetable> findByFacilityAndDay(Facility facility, FacilityTimetable.Day day);
}
