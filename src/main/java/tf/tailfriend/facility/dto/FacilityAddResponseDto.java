package tf.tailfriend.facility.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacilityAddResponseDto {

    private Integer id;
    private String name;
    private String facilityType;
    private String tel;
    private String address;
    private String detailAddress;
    private String comment;
    private Double latitude;
    private Double longitude;
    private Double starPoint;
    private Integer reviewCount;
    private LocalDateTime createdAt;
    private List<FacilityTimetableDto> timetables;
    private List<FacilityImageDto> images;

    // 내부 클래스: 시간표 DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FacilityTimetableDto {
        private String day;
        private String openTime;
        private String closeTime;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FacilityImageDto {
        private Integer id;
        private String url;
    }
}
