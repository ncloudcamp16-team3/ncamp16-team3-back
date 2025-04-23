package tf.tailfriend.user.entity.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import tf.tailfriend.file.entity.File.FileType;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterPetPhotoDto {
    private FileType type;
    private String path;
    private boolean thumbnail;
    private String originName;

}