package tf.tailfriend.facility.entity.dto.ResponseForReserve;

import lombok.*;

import tf.tailfriend.reserve.dto.Card;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacilityCard implements Card {

    private Integer id;
    private String category;
    private String name;
    private Double rating;
    private Integer reviewCount;
    private Double distance;
    private String address;
    private List<TimeRangeForWeek> timeRangeForWeek;
    private String image;

}
