package tf.tailfriend.board.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchRequestDto {
    private int boardTypeId;
    private String keyword;
    private int page;
    private int size;
}
