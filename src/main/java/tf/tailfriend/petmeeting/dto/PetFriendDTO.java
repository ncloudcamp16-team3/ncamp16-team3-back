package tf.tailfriend.petmeeting.dto;

import lombok.Builder;
import lombok.Data;
import tf.tailfriend.pet.entity.Pet;
import tf.tailfriend.pet.entity.PetPhoto;

import java.util.List;
import java.util.stream.Collectors;

@Data
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
    private List<PetPhotoDTO> photos;
    private Integer thumbnail;
    private Double distance;

    public static PetFriendDTO buildByEntity(Pet pet) {
        List<PetPhotoDTO> photoDTOs = pet.getPhotos().stream()
                .map(PetPhotoDTO::buildByEntity)
                .collect(Collectors.toList());

        Integer thumbnailId = pet.getPhotos().stream()
                .filter(PetPhoto::isThumbnail)
                .map(photo -> photo.getFile().getId())
                .findFirst()
                .orElse(null);

        return PetFriendDTO.builder()
                .id(pet.getId())
                .name(pet.getName())
                .gender(pet.getGender())
                .birth(pet.getBirth())
                .weight(pet.getWeight())
                .info(pet.getInfo())
                .neutered(pet.getNeutered())
                .activityStatus(pet.getActivityStatus().toString())
                .owner(OwnerDTO.buildByEntity(pet.getUser()))
                .photos(photoDTOs)
                .thumbnail(thumbnailId)
                .build();
    }
}
