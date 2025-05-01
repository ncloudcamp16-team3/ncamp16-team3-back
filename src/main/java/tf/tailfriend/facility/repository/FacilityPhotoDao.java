package tf.tailfriend.facility.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.facility.entity.FacilityPhoto;

import java.util.List;

public interface FacilityPhotoDao extends JpaRepository<FacilityPhoto, Integer> {

    List<FacilityPhoto> findByFacilityId(Integer id);
}
