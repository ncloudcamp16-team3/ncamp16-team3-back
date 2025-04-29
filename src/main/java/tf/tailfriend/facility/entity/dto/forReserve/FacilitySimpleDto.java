package tf.tailfriend.facility.entity.dto.forReserve;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FacilitySimpleDto {

    private Integer id;
    private String category;
    private String name;
    private Double starPoint;
    private Integer reviewCount;
    private Double distance;
    private String address;

}

