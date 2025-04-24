package tf.tailfriend.user.entity.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import tf.tailfriend.user.distance.Distance;
import tf.tailfriend.user.entity.SnsType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDto {

    private Integer id;
    private String nickname;
    private String address;
    private String dongName;
    private Double latitude;
    private Double longitude;
    private String Path;
    private Distance distance;
}
