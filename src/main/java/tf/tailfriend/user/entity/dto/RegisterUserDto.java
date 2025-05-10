package tf.tailfriend.user.entity.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserDto {

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 8, message = "닉네임은 2~8자여야 합니다.")
    private String nickname;

    @NotBlank(message = "SNS 계정 ID는 필수입니다.")
    private String snsAccountId;

    @NotNull(message = "SNS 유형 ID는 필수입니다.")
    private Integer snsTypeId;

    private Integer fileId;

    @Valid
    @NotEmpty(message = "반려동물 정보는 최소 1개 이상 입력해야 합니다.")
    private List<RegisterPetDto> pets;
}
