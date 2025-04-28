//package tf.tailfriend.facility.repository;
//
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//import tf.tailfriend.facility.entity.Review;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Repository
//public interface ReviewDao extends JpaRepository<Review, Integer> {
//
//    // 시설 ID로 리뷰 조회
//    List<Review> findByFacilityId(Integer facilityId);
//
//    // 시설 ID로 리뷰 페이징 조회
//    Page<Review> findByFacilityId(Integer facilityId, Pageable pageable);
//
//    // 사용자 ID로 리뷰 조회
//    List<Review> findByUserId(Integer userId);
//
//    // 사용자 ID로 리뷰 페이징 조회
//    Page<Review> findByUserId(Integer userId, Pageable pageable);
//
//    // 별점으로 리뷰 조회
//    List<Review> findByStarPoint(Integer starPoint);
//
//    // 시설별 별점 평균 및 리뷰 수 조회
//    @Query("SELECT r.facility.id, AVG(r.starPoint), COUNT(r) FROM Review r GROUP BY r.facility.id")
//    List<Object[]> findStarPointAverageAndCountByFacility();
//
//    // 특정 시설의 별점 평균 조회
//    @Query("SELECT AVG(r.starPoint) FROM Review r WHERE r.facility.id = :facilityId")
//    Double findAverageStarPointByFacilityId(@Param("facilityId") Integer facilityId);
//
//    // 날짜 범위 내 리뷰 조회
//    List<Review> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
//
//    // 최근 등록된 리뷰 조회
//    @Query("SELECT r FROM Review r ORDER BY r.createdAt DESC")
//    List<Review> findRecentReviews(Pageable pageable);
//
//    // 특정 시설의 리뷰 중 별점 높은 순으로 조회
//    @Query("SELECT r FROM Review r WHERE r.facility.id = :facilityId ORDER BY r.starPoint DESC")
//    List<Review> findTopReviewsByFacilityId(@Param("facilityId") Integer facilityId, Pageable pageable);
//
//    // 댓글이 있는 리뷰만 조회
//    @Query("SELECT r FROM Review r WHERE r.comment IS NOT NULL AND LENGTH(r.comment) > 0")
//    List<Review> findReviewsWithComments();
//
//    // 특정 타입의 시설에 대한 리뷰 조회
//    @Query("SELECT r FROM Review r JOIN r.facility f JOIN f.facilityType ft WHERE ft.name = :typeName")
//    List<Review> findReviewsByFacilityType(@Param("typeName") String typeName);
//}