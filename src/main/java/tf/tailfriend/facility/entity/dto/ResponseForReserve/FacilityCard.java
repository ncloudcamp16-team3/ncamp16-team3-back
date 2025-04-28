package tf.tailfriend.facility.entity.dto.ResponseForReserve;

import lombok.*;

import tf.tailfriend.facility.entity.FacilityTimetable;
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
//    private String openTime;
//    private String closeTime;
//    private String image;

    @Override
    public String toString() {
        return "FacilityCard{" +
                "id=" + id +
                ", category='" + category + '\'' +
                ", name='" + name + '\'' +
                ", rating=" + rating +
                ", reviewCount=" + reviewCount +
                ", distance=" + distance +
                ", address='" + address + '\'' +
//                ", openTime='" + openTime + '\'' +
//                ", closeTime='" + closeTime + '\'' +
                '}';
    }
}
