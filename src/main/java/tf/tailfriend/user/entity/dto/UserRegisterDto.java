package tf.tailfriend.user.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterDto {
    private String nickname;
    private String snsAccountId;
    private Integer snsTypeId;
    private String address;
    private String detailAddress;
    private String dongName;
    private Double latitude;
    private Double longitude;

    private List<PetRegisterDto> pets;
}
