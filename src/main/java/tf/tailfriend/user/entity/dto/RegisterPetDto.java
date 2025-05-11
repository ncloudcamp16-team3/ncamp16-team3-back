package tf.tailfriend.user.entity.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tf.tailfriend.pet.entity.Pet.ActivityStatus;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterPetDto {

    @NotNull(message = "반려동물 유형은 필수입니다.")
    private Integer petTypeId;

    @NotBlank(message = "이름은 필수입니다.")
    @Size(min = 1, max = 16, message = "이름은 1~16자여야 합니다.")
    private String name;

    @NotBlank(message = "성별은 필수입니다.") // 예: "MALE", "FEMALE"
    private String gender;

    @NotBlank(message = "생일은 필수입니다.") // 예: "2020-01-01"
    private String birth;

    @NotNull(message = "몸무게는 필수입니다.")
    @DecimalMax(value = "1000.0", inclusive = true, message = "몸무게는 최대 1000kg 이하여야 합니다.")
    private Double weight;

    @Size(max = 255, message = "소개는 255자 이내로 작성해주세요.")
    private String info;

    private boolean neutered; // 불린 값은 기본값이 있으므로 일반적으로 @NotNull 불필요

    @NotNull(message = "활동 상태는 필수입니다.")
    private ActivityStatus activityStatus;

    @Valid
    @NotNull(message = "사진 목록은 null일 수 없습니다.")
    @Size(max = 6, message = "사진은 최대 6장까지 등록할 수 있습니다.") // 최대 6장까지 허용
    private List<RegisterPetPhotoDto> photos;
}
