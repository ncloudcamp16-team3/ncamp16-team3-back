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
    private String name;
    private String tel;
    private String comment;
    private Double starPoint;
    private Integer reviewCount;
    private String address;
    private String detailAddress;
    private Double latitude;
    private Double longitude;
    private LocalDateTime createdAt;
    private Integer page;
    private boolean last;

    // Related entity info
    private FacilityTypeDto facilityType;
    private List<FacilityPhotoDto> photos;
    private List<FacilityTimetableDto> timetables;
    private List<ReviewDto> reviews;

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
        private Time openTime;
        private Time closeTime;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewDto {
        private Integer id;
        private Integer userId;
        private String nickName;  // Assuming you want to include this
        private String comment;
        private Integer starPoint;
        private LocalDateTime createdAt;
    }

    // Conversion from Entity
    public static FacilityDetailResponseDto from(Facility facility, List<ReviewDto> reviews, Integer responsePage, boolean responseLastPage) {
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
                .page(responsePage)
                .last(responseLastPage)
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
                                .openTime(timetable.getOpenTime())
                                .closeTime(timetable.getCloseTime())
                                .build())
                        .collect(Collectors.toList()))
                .reviews(reviews)
                .build();
    }
}
