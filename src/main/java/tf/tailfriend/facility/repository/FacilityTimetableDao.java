package tf.tailfriend.facility.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tf.tailfriend.facility.entity.FacilityTimetable;

import java.util.Collection;
import java.util.List;

@Repository
public interface FacilityTimetableDao extends JpaRepository<FacilityTimetable, Integer> {

    List<FacilityTimetable> findByFacilityId(Integer id);

    List<FacilityTimetable> findByFacility_IdInAndDay(Collection<Integer> facility_id, FacilityTimetable.Day dayEnum);
}
