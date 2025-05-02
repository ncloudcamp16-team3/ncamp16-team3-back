package tf.tailfriend.petsta.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PetstaCommentResponseDto {
    private Integer id;
    private String content;
    private String userName;
    private Integer userId;
    private String userPhoto;
    private String createdAt;
    private Integer parentId;
    private Integer replyCount;
    private Boolean isVisited;
    private MentionDto mention;
    boolean deleted;
}
