package tf.tailfriend.pet.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetResponseDto {
    private Integer id;
    private Integer ownerId;
    private Integer petTypeId;
    private String name;
    private String gender;
    private String birth;
    private Double weight;
    private String info;
    private Boolean neutered;
    private String activityStatus;
    private List<PetPhotoDto> photos;
}
