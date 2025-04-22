package tf.tailfriend.user.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tf.tailfriend.file.entity.File.FileType;
import tf.tailfriend.pet.entity.PetPhoto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterPetPhotoDto {
    private FileType type;
    private String path;
    private boolean thumbnail;
    private String originName;

    public static RegisterPetPhotoDto buildByEntity(PetPhoto photo) {
        return RegisterPetPhotoDto.builder()
                .type(photo.getFile().getType())
                .path(photo.getFile().getPath())
                .thumbnail(photo.isThumbnail())
                .build();
    }

}