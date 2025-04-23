package tf.tailfriend.petmeeting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tf.tailfriend.pet.entity.PetPhoto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetPhotoDTO {
    private Integer id;
    private String path;
    private boolean thumbnail;

    public static PetPhotoDTO buildByEntity(PetPhoto photo) {
        return PetPhotoDTO.builder()
                .id(photo.getFile().getId())
                .path(photo.getFile().getPath())
                .thumbnail(photo.isThumbnail())
                .build();
    }
}