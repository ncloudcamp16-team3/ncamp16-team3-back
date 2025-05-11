package tf.tailfriend.reserve.dto;

import lombok.Builder;
import lombok.Getter;
import tf.tailfriend.facility.dto.ReviewResponseDto;
import tf.tailfriend.facility.entity.Review;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReserveDetailResponseDto {
    private Integer id;
    private String name;
    private String address;
    private String type;
    private Boolean status;
    private Integer facilityId;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private Integer amount;
    private String image;
    private Double latitude;
    private Double longitude;
    private ReviewResponseDto reviewDto;

    public static ReviewResponseDto reviewDtoFromEntity (Review review) {
         return ReviewResponseDto.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .userName(review.getUser().getNickname())
                .comment(review.getComment())
                .starPoint(review.getStarPoint())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
