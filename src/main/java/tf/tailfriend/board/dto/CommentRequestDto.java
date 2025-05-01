package tf.tailfriend.board.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class CommentRequestDto {
    Integer commentId;
    String comment;
    Integer boardId;
    Integer userId;
}
