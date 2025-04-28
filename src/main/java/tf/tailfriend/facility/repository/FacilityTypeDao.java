package tf.tailfriend.facility.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.facility.entity.FacilityType;

public interface FacilityTypeDao extends JpaRepository<FacilityType, Integer> {
}
