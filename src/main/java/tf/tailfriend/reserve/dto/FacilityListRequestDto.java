package tf.tailfriend.reserve.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FacilityListRequestDto {

    private double userLatitude;

    private double userLongitude;

    private String category;

    private String sortBy;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 10;
}