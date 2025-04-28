package tf.tailfriend.facility.entity.dto.ResponseForReserve;

import org.springframework.beans.factory.annotation.Value;

public interface FacilityWithDistanceProjection {

    @Value("#{target.id}")
    Integer getId();

    @Value("#{target.category}")
    String getCategory();

    @Value("#{target.name}")
    String getName();

    @Value("#{target.starPoint}")
    Double getStarPoint();

    @Value("#{target.reviewCount}")
    Integer getReviewCount();

    @Value("#{target.distance}")
    Double getDistance();

    @Value("#{target.address}")
    String getAddress();
}