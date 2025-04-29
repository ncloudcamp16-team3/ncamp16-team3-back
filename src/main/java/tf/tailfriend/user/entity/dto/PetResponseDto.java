package tf.tailfriend.user.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetResponseDto {
    private Integer id;
    private String name;
    private String birth;
    private String type;
    private String gender;
    private Double weight;
    private String info;
    private Boolean neutered;
    private String profileImageUrl;
    private List<String> petPhotoUrls;


}

