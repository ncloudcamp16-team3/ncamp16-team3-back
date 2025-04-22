package tf.tailfriend.petmeeting.dto;

import lombok.Builder;
import lombok.Getter;
import tf.tailfriend.pet.entity.Pet;
import tf.tailfriend.user.entity.dto.PetPhotoDto;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class PetFriendDTO {
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
    private Integer thumbnail;

    public static PetFriendDTO buildByEntity(Pet pet) {
        return PetFriendDTO.builder()
                .id(pet.getId())
                .name(pet.getName())
                .gender(pet.getGender())
                .birth(pet.getBirth())
                .weight(pet.getWeight())
                .info(pet.getInfo())
                .neutered(pet.getNeutered())
                .activityStatus(pet.getActivityStatus().name())
                .owner(OwnerDTO.buildByEntity(pet.getUser()))
                .photos(pet.getPhotos().stream()
                        .map(PetPhotoDto::buildByEntity)
                        .collect(Collectors.toList()))
                .thumbnail(pet.getPhotos().stream()
                        .filter(photo -> photo.isThumbnail())
                        .map(photo -> photo.getFile().getId())
                        .findFirst()
                        .orElse(null))
                .build();
    }
}
