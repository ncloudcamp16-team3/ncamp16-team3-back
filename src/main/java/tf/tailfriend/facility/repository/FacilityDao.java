package tf.tailfriend.facility.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tf.tailfriend.facility.entity.FacilityType;
import tf.tailfriend.facility.entity.dto.ResponseForReserve.FacilityCard;
import tf.tailfriend.facility.entity.Facility;

@Repository
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

    @Query("""
    SELECT new tf.tailfriend.facility.entity.dto.ResponseForReserve.FacilityCard(
        f.id,
        ft.name,
        f.name,
        f.starPoint,
        f.reviewCount,
        function('ST_DISTANCE_SPHERE', POINT(:lng, :lat), POINT(f.longitude, f.latitude)),
        f.address,
        ftt.timetables,
        fi.path
    )
    FROM Facility f
    JOIN f.facilityType ft
    JOIN f.facilityTimetables ftt ON ftt.day = :day
    JOIN f.photos fp
    JOIN fp.file fi
    WHERE ft.name = :category
      AND fp.thumbnail = true
""")
    Slice<FacilityCard> findByCategoryWithFacilityTypeAndThumbnail(
            @Param("category") String category,
            @Param("day") tf.tailfriend.facility.entity.FacilityTimetable.Day day,
            @Param("lat") double lat,
            @Param("lng") double lng,
            Pageable pageable
    );
}
