package tf.tailfriend.facility.dto;

import lombok.Builder;
import lombok.Getter;

import tf.tailfriend.facility.entity.Facility;
import tf.tailfriend.global.service.DateTimeFormatProvider;
import tf.tailfriend.global.service.DistanceFormatProvider;
import tf.tailfriend.reserve.dto.Card;

import java.util.List;

@Getter
@Builder
public class FacilityCardForReserve implements Card {

    private int id;
    private String category;
    private String name;
    private double rating;
    private int reviewCount;
    private double distance;
    private String address;
    private String openDays;
    private List<TimeRangeForWeek> timeRangeForWeek;
    private String image;

    public static FacilityCardForReserve fromEntity(
            Facility facility,
            double userLatitude,
            double userLongitude,
            DistanceFormatProvider distanceProvider,
            DateTimeFormatProvider timeProvider) {
        return FacilityCardForReserve.builder()
                .id(facility.getId())
                .category(facility.getFacilityType().getName())
                .name(facility.getName())
                .rating(facility.getStarPoint())
                .reviewCount(facility.getReviewCount())
                .distance(distanceProvider.calculateDistance(
                        userLatitude,
                        userLongitude,
                        facility.getLatitude(),
                        facility.getLongitude()))
                .address(facility.getAddress())
                .openDays(timeProvider.simplifyDays(facility.getTimetables()))
                .timeRangeForWeek(TimeRangeForWeek.fromEntityList(facility.getTimetables(), timeProvider))
                .image(facility.getPhotos().isEmpty() ? null :
                        facility.getPhotos().get(0).getFile().getPath())
                .build();
    }
}
