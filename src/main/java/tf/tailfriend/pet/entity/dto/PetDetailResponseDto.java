package tf.tailfriend.pet.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetDetailResponseDto {
    private Integer ownerId;
    private Integer id;
    private String name;
    private String type;
    private String birthDate;
    private String gender;
    private Boolean isNeutered;
    private Double weight;
    private String introduction;
    private String activityStatus;
    private List<PetPhotoDto> photos;
}