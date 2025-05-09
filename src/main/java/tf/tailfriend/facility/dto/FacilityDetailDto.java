package tf.tailfriend.facility.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FacilityDetailDto {

    private FacilityResponseDto facility;
    private List<ReviewResponseDto> reviews;
    private List<Object[]> ratingRatio;
    private Integer userId;
}
