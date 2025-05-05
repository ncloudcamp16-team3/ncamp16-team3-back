package tf.tailfriend.user.entity.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "파일 유형은 필수입니다.")
    private FileType type;

    @NotBlank(message = "파일 경로는 필수입니다.")
    private String path;

    private boolean thumbnail; // boolean은 기본값 존재하므로 @NotNull 필요 없음

    @NotBlank(message = "원본 파일명은 필수입니다.")
    private String originName;
}