package tf.tailfriend.facility.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tf.tailfriend.facility.entity.Review;

@Repository
public interface ReviewDao extends JpaRepository<Review, Integer> {

    /**
     * Find reviews by facility ID with user information
     * Returns a Slice for pagination
     */
    @Query("SELECT r FROM Review r " +
            "JOIN FETCH r.user u " +
            "WHERE r.facility.id = :facilityId " +
            "ORDER BY r.createdAt DESC")
    Slice<Review> findReviewsByFacilityId(@Param("facilityId") Integer facilityId, Pageable pageable);
}
