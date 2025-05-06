package tf.tailfriend.facility.entity.dto.forReserve;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tf.tailfriend.facility.entity.Facility;
import tf.tailfriend.facility.entity.FacilityTimetable;
import tf.tailfriend.file.entity.File;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityDetailResponseDto {
    // Basic facility info
    private Integer id;
    private String category;
    private String name;
    private String tel;
    private String comment;
    private Double starPoint;
    private Integer reviewCount;
    private String address;
    private String detailAddress;
    private Double latitude;
    private Double longitude;
    private String openTimeRange;
    private LocalDateTime createdAt;
    private String thumbnail;

    // Related entity info
    private FacilityTypeDto facilityType;
    private List<FacilityPhotoDto> photos;
    private List<FacilityTimetableDto> timetables;

    // DTOs for related entities
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacilityTypeDto {
        private Integer id;
        private String name;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacilityPhotoDto {
        private Integer fileId;
        private String path;
        private File.FileType type;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacilityTimetableDto {
        private Integer id;
        private FacilityTimetable.Day day;
        private String openTime;
        private String closeTime;
    }
    public static FacilityDetailResponseDto from(Facility facility) {
        return FacilityDetailResponseDto.builder()
                .id(facility.getId())
                .name(facility.getName())
                .tel(facility.getTel())
                .comment(facility.getComment())
                .starPoint(facility.getStarPoint())
                .reviewCount(facility.getReviewCount())
                .address(facility.getAddress())
                .detailAddress(facility.getDetailAddress())
                .latitude(facility.getLatitude())
                .longitude(facility.getLongitude())
                .createdAt(facility.getCreatedAt())
                .facilityType(FacilityTypeDto.builder()
                        .id(facility.getFacilityType().getId())
                        .name(facility.getFacilityType().getName())
                        .build())
                .photos(facility.getPhotos().stream()
                        .map(photo -> FacilityPhotoDto.builder()
                                .fileId(photo.getFile().getId())
                                .path(photo.getFile().getPath())
                                .type(photo.getFile().getType())
                                .build())
                        .collect(Collectors.toList()))
                .timetables(facility.getTimetables().stream()
                        .map(timetable -> FacilityTimetableDto.builder()
                                .id(timetable.getId())
                                .day(timetable.getDay())
                                .openTime(timetable.getOpenTime().toString())
                                .closeTime(timetable.getCloseTime().toString())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
