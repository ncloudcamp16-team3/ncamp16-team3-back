package tf.tailfriend.facility.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tf.tailfriend.facility.entity.Facility;
import tf.tailfriend.facility.entity.Review;

import java.util.List;
@Repository
public interface ReviewDao extends JpaRepository<Review, Integer> {

    // 시설별로 최신순 리뷰 조회
    List<Review> findByFacilityOrderByCreatedAtDesc(Facility facility);

    // 시설 ID로 직접 조회
    List<Review> findByFacilityIdOrderByCreatedAtDesc(Integer facilityId);

    // 평균 별점을 계산하는 쿼리 메서드
    @Query("SELECT AVG(r.starPoint) FROM Review r WHERE r.facility.id = :facilityId")
    Double calculateAverageStarPoint(@Param("facilityId") Integer facilityId);

    // 별점별 개수를 계산하는 쿼리 메서드
    @Query("SELECT r.starPoint, COUNT(r) FROM Review r WHERE r.facility.id = :facilityId GROUP BY r.starPoint")
    List<Object[]> countReviewsByStarPoint(@Param("facilityId") Integer facilityId);

    @Query("SELECT COALESCE(AVG(r.starPoint), 0.0) FROM Review r WHERE r.facility.id = :facilityId")
    Double calculateAverageStarPointByFacilityId(@Param("facilityId") Integer facilityId);
}
