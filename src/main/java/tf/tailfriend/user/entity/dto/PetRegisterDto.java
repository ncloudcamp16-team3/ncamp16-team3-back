package tf.tailfriend.user.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PetRegisterDto {
    private Integer petTypeId;
    private String name;
    private String gender;
    private String birth;
    private Double weight;
    private String info;
    private boolean neutured;

    // ✅ 사진 정보 추가
    private List<PetPhotoDto> petPhotos;
}
