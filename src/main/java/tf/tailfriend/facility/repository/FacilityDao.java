package tf.tailfriend.facility.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tf.tailfriend.facility.entity.Facility;

@Repository
public interface FacilityDao {

    @Query("SELECT f FROM Facility f LEFT JOIN FETCH f.facilityType ft LEFT JOIN FETCH f.photos LEFT JOIN FETCH f.timetables WHERE ft.name = :category")
    Slice<Facility> findAllFacilitiesForReserve(@Param("category") String category, Pageable pageable);

}
