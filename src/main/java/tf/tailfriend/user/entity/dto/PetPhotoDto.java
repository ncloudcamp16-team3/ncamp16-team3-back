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
public class PetPhotoDto {
    private FileType type;
    private String path;
    private String uuid;
    private boolean thumbnail;

    public static PetPhotoDto buildByEntity(PetPhoto photo) {
        return PetPhotoDto.builder()
                .type(photo.getFile().getType())
                .path(photo.getFile().getPath())
                .uuid(photo.getFile().getUuid())
                .thumbnail(photo.isThumbnail())
                .build();
    }
}