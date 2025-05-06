package tf.tailfriend.facility.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tf.tailfriend.facility.entity.FacilityType;
import tf.tailfriend.facility.entity.Facility;
import tf.tailfriend.facility.entity.dto.forReserve.FacilityWithDistanceProjection;

import java.util.Optional;

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

    //         fi.path AS image
    // JOIN facility_photos fp ON fp.facility_id = f.id
    //    JOIN files fi ON fi.id = fp.file_id
    // AND fp.thumbnail = true
    @Query("SELECT DISTINCT " +
            "f.id AS id, " +
            "ft.name AS category, " +
            "f.name AS name, " +
            "f.starPoint AS starPoint, " +
            "f.reviewCount AS reviewCount, " +
            "ROUND(function('ST_DISTANCE_SPHERE', function('POINT', :lng, :lat), function('POINT', f.longitude, f.latitude)), 0) AS distance, " +
            "f.address AS address " +
            "FROM Facility f JOIN f.facilityType ft " +
            "WHERE f.facilityType.name = :category " +
            "ORDER BY ROUND(function('ST_DISTANCE_SPHERE', function('POINT', :lng, :lat), function('POINT', f.longitude, f.latitude)), 0)")
    Slice<FacilityWithDistanceProjection> findByCategoryWithSortByDistance(@Param("lng") Double lng, @Param("lat") Double lat, @Param("category") String category, Pageable pageable);

    @Query("SELECT DISTINCT " +
            "f.id AS id, " +
            "ft.name AS category, " +
            "f.name AS name, " +
            "f.starPoint AS starPoint, " +
            "f.reviewCount AS reviewCount, " +
            "ROUND(function('ST_DISTANCE_SPHERE', function('POINT', :lng, :lat), function('POINT', f.longitude, f.latitude)), 0) AS distance, " +
            "f.address AS address " +
            "FROM Facility f JOIN f.facilityType ft " +
            "WHERE f.facilityType.name = :category " +
            "ORDER BY f.starPoint DESC")
    Slice<FacilityWithDistanceProjection> findByCategoryWithSortByStarPoint(@Param("lng") Double lng, @Param("lat") Double lat, @Param("category") String category, Pageable pageable);

    /**
     * Fetch a facility by ID with all related entities eagerly loaded
     */
    @Query("SELECT f FROM Facility f " +
            "LEFT JOIN FETCH f.facilityType " +
            "LEFT JOIN FETCH f.photos fp " +
            "LEFT JOIN FETCH fp.file " +
            "LEFT JOIN FETCH f.timetables " +
            "WHERE f.id = :facilityId")
    Facility findByIdWithDetails(@Param("facilityId") Integer facilityId);
}
