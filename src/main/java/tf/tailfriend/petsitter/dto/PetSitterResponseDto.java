package tf.tailfriend.petsitter.dto;

import lombok.*;
import tf.tailfriend.petsitter.entity.PetSitter;

import java.time.LocalDateTime;

@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PetSitterResponseDto {

    private Integer id;
    private String nickname; // 사용자의 닉네임
    private String age;
    private String houseType;
    private String comment;
    private Boolean grown;
    private String petCount;
    private Boolean sitterExp;
    private String imagePath; // 파일 경로
    private LocalDateTime createdAt;
    private LocalDateTime applyAt;

    // Entity를 DTO로 변환하는 정적 메서드
    public static PetSitterResponseDto fromEntity(PetSitter petSitter) {
        return PetSitterResponseDto.builder()
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
                .build();
    }
}
