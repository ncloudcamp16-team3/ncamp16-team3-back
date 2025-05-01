package tf.tailfriend.facility.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.facility.entity.Facility;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tf.tailfriend.facility.entity.FacilityTimetable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface FacilityTimetableDao extends JpaRepository<FacilityTimetable, Integer> {

    List<FacilityTimetable> findByFacilityId(Integer id);

    Optional<FacilityTimetable> findByFacilityAndDay(Facility facility, FacilityTimetable.Day day);

    List<FacilityTimetable> findByFacility_IdInAndDay(Collection<Integer> facility_id, FacilityTimetable.Day dayEnum);

}
