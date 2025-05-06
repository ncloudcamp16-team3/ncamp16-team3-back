package tf.tailfriend.petsitter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tf.tailfriend.petsitter.entity.PetSitter.PetCount;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetSitterRequestDto {

    private Integer userId;
    private Integer petTypeId;
    private List<Integer> petTypeIds;
    private String petTypesFormatted;

    private String age;
    private String houseType;
    private String comment;
    private Boolean grown;
    private PetCount petCount;
    private Boolean sitterExp;
}