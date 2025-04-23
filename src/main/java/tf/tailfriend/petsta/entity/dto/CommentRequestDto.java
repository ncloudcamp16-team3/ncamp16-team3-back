package tf.tailfriend.petsta.entity.dto;// Comment 요청 DTO
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentRequestDto {
    private String content;
    private Integer parentId; // 부모 댓글 ID (없으면 null)
}
