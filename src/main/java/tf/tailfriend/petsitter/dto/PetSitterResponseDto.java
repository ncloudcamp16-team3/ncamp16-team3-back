package tf.tailfriend.petsitter.dto;

import lombok.*;
import tf.tailfriend.petsitter.entity.PetSitter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PetSitterResponseDto {

    private Integer id;
    private String nickname;
    private String age;
    private String houseType;
    private String comment;
    private Boolean grown;
    private String petCount;
    private Boolean sitterExp;
    private String imagePath;
    private LocalDateTime createdAt;
    private LocalDateTime applyAt;
    private String status;

    private String petTypesFormatted;
    private List<String> petTypes;


    public static PetSitterResponseDto fromEntity(PetSitter petSitter) {
        PetSitterResponseDto dto = PetSitterResponseDto.builder()
                .id(petSitter.getId())
                .nickname(petSitter.getUser().getNickname())
                .age(petSitter.getAge())
                .houseType(petSitter.getHouseType())
                .comment(petSitter.getComment())
                .grown(petSitter.getGrown())
                .petCount(petSitter.getPetCount() != null ? petSitter.getPetCount().getValue() : null)
                .sitterExp(petSitter.getSitterExp())
                .imagePath(petSitter.getFile().getPath())
                .createdAt(petSitter.getCreatedAt())
                .applyAt(petSitter.getApplyAt() != null ? petSitter.getApplyAt() : null)
                .status(petSitter.getStatus().toString())
                .petTypesFormatted(petSitter.getPetTypesFormatted())
                .build();

        return dto;
    }
}