package tf.tailfriend.facility.entity.dto.forReserve;

import lombok.*;

import tf.tailfriend.reserve.dto.Card;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacilityCardResponseDto implements Card {

    private Integer id;
    private String category;
    private String name;
    private Double rating;
    private Integer reviewCount;
    private Integer distance;
    private String tel;
    private String address;
    private String openTimeRange;
    private boolean isOpened;
    private String image;

    @Override
    public String toString() {
        return "FacilityCardResponseDto{" +
                "id=" + id +
                ", category='" + category + '\'' +
                ", name='" + name + '\'' +
                ", rating=" + rating +
                ", reviewCount=" + reviewCount +
                ", distance=" + distance +
                ", tel='" + tel + '\'' +
                ", address='" + address + '\'' +
                ", openTime='" + openTimeRange + '\'' +
                ", isOpened=" + isOpened + '\'' +
                ", image=" + image + '\'' +
                '}';
    }
}
