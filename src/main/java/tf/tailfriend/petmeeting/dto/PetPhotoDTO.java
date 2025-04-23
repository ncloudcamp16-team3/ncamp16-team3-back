package tf.tailfriend.petmeeting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetPhotoDTO {
    private Integer id;
    private String path;
    private boolean thumbnail;

    public PetPhotoDTO(Integer id, String path, Boolean thumbnail) {
        this.id = id;
        this.path = path;
        this.thumbnail = thumbnail;
    }
}