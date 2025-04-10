package tf.tailfriend.user.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tf.tailfriend.user.entity.Files.FileType;
import tf.tailfriend.user.entity.Pets.ActivityStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewDto {

    private String nickname;
    private String snsAccountId;
    private Integer snsTypeId;
    private Integer fileId;

    private Integer ownerId;
    private Integer petTypeId;
    private String name;
    private String gender;
    private String birth;
    private Double weight;
    private String info;
    private boolean neutured;
    private ActivityStatus activityStatus;

    private FileType type;
    private String path;
    private String uuid;

    private boolean thumbnail;

}
