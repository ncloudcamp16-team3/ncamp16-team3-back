package tf.tailfriend.facility.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.facility.entity.Facility;
import tf.tailfriend.facility.entity.FacilityType;

public interface FacilityDao extends JpaRepository<Facility, Integer> {

    Page<Facility> findByFacilityType(FacilityType facilityTypeId, Pageable pageable);

    Page<Facility> findByNameContaining(String name, Pageable pageable);
    Page<Facility> findByFacilityTypeAndNameContaining(FacilityType facilityType, String name, Pageable pageable);

    Page<Facility> findByAddressContaining(String address, Pageable pageable);
    Page<Facility> findByFacilityTypeAndAddressContaining(FacilityType facilityType, String address, Pageable pageable);

    Page<Facility> findByTelContaining(String tel, Pageable pageable);
    Page<Facility> findByFacilityTypeAndTelContaining(FacilityType facilityType, String tel, Pageable pageable);

    Page<Facility> findByCommentContaining(String comment, Pageable pageable);
    Page<Facility> findByFacilityTypeAndCommentContaining(FacilityType facilityType, String comment, Pageable pageable);
}
