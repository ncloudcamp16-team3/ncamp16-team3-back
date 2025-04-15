package tf.tailfriend.user.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tf.tailfriend.pet.entity.Pet.ActivityStatus;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PetRegisterDto {

    private Integer petTypeId;
    private String name;
    private String gender;
    private String birth;
    private Double weight;
    private String info;
    private boolean neutered;
    private ActivityStatus activityStatus;

    private List<PetPhotoDto> photos;
}
