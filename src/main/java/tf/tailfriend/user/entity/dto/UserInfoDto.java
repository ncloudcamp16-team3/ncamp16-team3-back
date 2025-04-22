package tf.tailfriend.user.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import tf.tailfriend.user.entity.SnsType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDto {

    private Integer id;
    private String nickname;
    private String snsAccountId;
    private String address;
    private String dongName;
    private Double latitude;
    private Double longitude;
    private String Path;

}
