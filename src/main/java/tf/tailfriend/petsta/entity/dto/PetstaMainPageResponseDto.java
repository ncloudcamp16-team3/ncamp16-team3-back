// PetstaMainPageResponseDto.java
package tf.tailfriend.petsta.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PetstaMainPageResponseDto {
    private List<PetstaPostResponseDto> posts;
    private List<PetstaUpdatedUserDto> followings;
}
