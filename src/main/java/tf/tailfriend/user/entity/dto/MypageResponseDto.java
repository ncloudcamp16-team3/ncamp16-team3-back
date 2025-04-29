package tf.tailfriend.user.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MypageResponseDto {
    private Integer userId;
    private String nickname;
    private String profileImageUrl;
    private List<PetResponseDto> pets;
    private boolean isSitter;
}
