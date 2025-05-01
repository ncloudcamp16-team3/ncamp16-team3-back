package tf.tailfriend.board.dto;

import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoardStatusDto {
    private boolean liked;
    private boolean bookmarked;
}
