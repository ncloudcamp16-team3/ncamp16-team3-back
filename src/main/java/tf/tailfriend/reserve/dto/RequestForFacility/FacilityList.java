package tf.tailfriend.reserve.dto.RequestForFacility;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FacilityList {

    private String day;

    private double userLatitude;

    private double userLongitude;

    private String category;

    private String sortBy;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 10;
}