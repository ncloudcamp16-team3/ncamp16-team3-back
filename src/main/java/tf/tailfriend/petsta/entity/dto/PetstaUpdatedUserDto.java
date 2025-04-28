package tf.tailfriend.petsta.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PetstaUpdatedUserDto {
    private Integer id;
    private String name;
    private String fileName;
    private Boolean isVisited;
}
