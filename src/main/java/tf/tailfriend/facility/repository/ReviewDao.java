package tf.tailfriend.facility.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tf.tailfriend.facility.entity.Review;

@Repository
public interface ReviewDao extends JpaRepository<Review, Integer> {
    @Query("SELECT COALESCE(AVG(r.starPoint), 0.0) FROM Review r WHERE r.facility.id = :facilityId")
    Double calculateAverageStarPointByFacilityId(@Param("facilityId") Integer facilityId);
}
