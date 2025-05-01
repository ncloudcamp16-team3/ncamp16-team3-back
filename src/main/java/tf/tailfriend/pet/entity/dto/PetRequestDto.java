package tf.tailfriend.pet.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetRequestDto {
    private String name;
    private String type;
    private String birthDate;
    private String gender;
    private Boolean isNeutered;
    private Double weight;
    private String introduction;
    private Integer mainPhotoIndex;
}
