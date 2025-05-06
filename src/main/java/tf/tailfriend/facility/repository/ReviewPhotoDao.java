package tf.tailfriend.facility.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.facility.entity.ReviewPhoto;

import java.util.List;

public interface ReviewPhotoDao extends JpaRepository<ReviewPhoto, Integer> {

    List<ReviewPhoto> findByReviewId(Integer reviewId);
}
