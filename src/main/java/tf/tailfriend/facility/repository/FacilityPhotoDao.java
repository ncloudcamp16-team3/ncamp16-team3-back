package tf.tailfriend.facility.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.facility.entity.FacilityPhoto;

public interface FacilityPhotoDao extends JpaRepository<FacilityPhoto, Integer> {
}
