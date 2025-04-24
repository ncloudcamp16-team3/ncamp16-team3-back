package tf.tailfriend.pet.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FindFriendRequestDto {
    private String activityStatus;
    private String dongName;
    private String distance;
    private int page = 0;
    private int size = 5;
    private double latitude;
    private double longitude;
}
