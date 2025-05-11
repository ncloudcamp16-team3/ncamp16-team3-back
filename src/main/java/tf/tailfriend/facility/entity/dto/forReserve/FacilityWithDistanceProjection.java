package tf.tailfriend.facility.entity.dto.forReserve;

import org.springframework.beans.factory.annotation.Value;

public interface FacilityWithDistanceProjection {

    @Value("#{target.id}")
    Integer getId();

    @Value("#{target.category}")
    String getCategory();

    @Value("#{target.name}")
    String getName();

    @Value("#{target.totalStarPoint}")
    Integer  getTotalStarPoint();

    @Value("#{target.reviewCount}")
    Integer getReviewCount();

    @Value("#{target.distance}")
    Integer getDistance();

    @Value("#{target.tel}")
    String getTel();

    @Value("#{target.address}")
    String getAddress();
}