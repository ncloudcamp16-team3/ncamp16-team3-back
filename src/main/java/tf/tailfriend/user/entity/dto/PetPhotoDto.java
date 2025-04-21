package tf.tailfriend.user.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tf.tailfriend.file.entity.File.FileType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PetPhotoDto {
    private FileType type;
    private String path;
    private String uuid;
    private boolean thumbnail;
    private String originFilename;
}