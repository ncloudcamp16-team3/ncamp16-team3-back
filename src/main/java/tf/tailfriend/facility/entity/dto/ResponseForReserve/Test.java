package tf.tailfriend.facility.entity.dto.ResponseForReserve;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class Test {
    private Integer id;
    private String category;
    private String name;
    private Double starPoint;
    private Integer reviewCount;
    private Double distance;
    private String address;
}
