package tf.tailfriend.pet.entity.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetFriendDto {
    private Integer id;
    private String name;
    private String gender;
    private String birth;
    private Double weight;
    private String info;
    private Boolean neutered;
    private String activityStatus;
    private OwnerDTO owner;
    private List<PetPhotoDto> photos;
    private Double distance;

    public PetFriendDto(Integer id, String name, String gender, String birth, Double weight, String info,
                        Boolean neutered, String activityStatus, Integer ownerId, String nickname,
                        String address, String dongName, Double latitude, Double longitude, Double distance) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.birth = birth;
        this.weight = weight;
        this.info = info;
        this.neutered = neutered;
        this.activityStatus = activityStatus;
        this.owner = OwnerDTO.builder()
                .id(ownerId)
                .nickname(nickname)
                .address(address)
                .dongName(dongName)
                .latitude(latitude)
                .longitude(longitude)
                .build();
        this.distance = distance;
    }

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OwnerDTO {
        private Integer id;
        private String nickname;
        private String address;
        private String dongName;
        private Double latitude;
        private Double longitude;
        private String distance;
    }
}
