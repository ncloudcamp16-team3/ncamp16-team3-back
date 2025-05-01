package tf.tailfriend.board.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BoardRequestDto {
    private Integer id;

    @NotBlank(message = "게시판 아이디는 필수입니다.")
    private Integer boardTypeId;

    @NotBlank(message = "게시글 제목은 필수입니다.")
    private String title;

    @NotBlank(message = "게시글 내용은 필수입니다.")
    private String content;
    private Integer authorId;
    private Integer price;
    private Boolean sell;
    private String address;
    private List<Integer> deleteFileIds = new ArrayList<>();
}

