package tf.tailfriend.facility.dto;

import lombok.*;
import tf.tailfriend.facility.entity.Facility;

import java.time.LocalDateTime;

@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FacilityResponseDto {

    private Integer id;
    private String facilityType;
    private String name;
    private String tel;
    private String comment;
    private Double starPoint;
    private Integer reviewCount;
    private String address;
    private String detailAddress;
    private String imagePath;
    private LocalDateTime createdAt;

    // Entity를 DTO로 변환하는 정적 메서드
    public static FacilityResponseDto fromEntity(Facility facility) {
        return FacilityResponseDto.builder()
                .id(facility.getId())
                .facilityType(facility.getFacilityType().getName())
                .name(facility.getName())
                .tel(facility.getTel())
                .comment(facility.getComment())
                .starPoint(facility.getStarPoint())
                .reviewCount(facility.getReviewCount())
                .address(facility.getAddress())
                .detailAddress(facility.getDetailAddress())
                .createdAt(facility.getCreatedAt())
                .build();
    }


}
