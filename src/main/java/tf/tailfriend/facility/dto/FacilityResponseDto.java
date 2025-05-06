package tf.tailfriend.facility.dto;

import lombok.*;
import tf.tailfriend.facility.entity.Facility;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
    private List<String> imagePaths;
    private LocalDateTime createdAt;
    private Double latitude;
    private Double longitude;

    private Map<String, OpeningHoursDto> openingHours;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OpeningHoursDto {
        private String openTime;
        private String closeTime;
        private Boolean isOpen;
    }

    // Entity를 DTO로 변환하는 정적 메서드
    public static FacilityResponseDto fromEntity(Facility facility) {
        Double starPoint = facility.getReviewCount() == 0
                ? 0.0
                : (double) facility.getTotalStarPoint() / facility.getReviewCount();

        return FacilityResponseDto.builder()
                .id(facility.getId())
                .facilityType(facility.getFacilityType().getName())
                .name(facility.getName())
                .tel(facility.getTel())
                .comment(facility.getComment())
                .starPoint(starPoint)
                .reviewCount(facility.getReviewCount())
                .address(facility.getAddress())
                .detailAddress(facility.getDetailAddress())
                .createdAt(facility.getCreatedAt())
                .latitude(facility.getLatitude())
                .longitude(facility.getLongitude())
                .build();
    }


}
