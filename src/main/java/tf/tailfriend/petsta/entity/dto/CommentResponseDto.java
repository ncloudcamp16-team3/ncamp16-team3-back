package tf.tailfriend.petsta.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CommentResponseDto {
    private Integer id;
    private String content;
    private String userName;
    private String userPhoto;
    private LocalDateTime createdAt;
    private Integer parentId;
    private Integer replyCount;
    private Boolean isView;
}
