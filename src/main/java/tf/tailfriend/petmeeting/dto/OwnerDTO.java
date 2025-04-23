package tf.tailfriend.petmeeting.dto;

import lombok.*;
import tf.tailfriend.user.entity.User;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerDTO {
    private Integer id;
    private String nickname;
    private String address;
    private String dongName;
    private Double latitude;
    private Double longitude;
    private String distance;

    public static OwnerDTO buildByEntity(User owner) {
        return OwnerDTO.builder()
                .id(owner.getId())
                .nickname(owner.getNickname())
                .address(owner.getAddress())
                .dongName(owner.getDongName())
                .latitude(owner.getLatitude())
                .longitude(owner.getLongitude())
                .distance(owner.getDistance() != null ? owner.getDistance().name() : null)
                .build();
    }
}
