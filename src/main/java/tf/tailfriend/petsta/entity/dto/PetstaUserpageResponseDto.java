package tf.tailfriend.petsta.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PetstaUserpageResponseDto {
    private Integer id;
    private String name;
    private String userPhoto;
    private Integer postCount;
    private Integer followerCount;
    private Integer followCount;
    private List<PetstaSimplePostDto> posts;
    private Boolean isFollow;
}
