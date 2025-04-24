package tf.tailfriend.pet.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetPhotoDto {
    private Integer id;
    private String path;
    private boolean thumbnail;

    public PetPhotoDto(Integer id, String path, Boolean thumbnail) {
        this.id = id;
        this.path = path;
        this.thumbnail = thumbnail;
    }
}