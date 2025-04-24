package tf.tailfriend.petsta.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PetstaFollowingUserDto {
    private Integer id;
    private String userName;
    private String userPhoto;
    private Boolean isFollow;
    private Boolean isVisited;
}
