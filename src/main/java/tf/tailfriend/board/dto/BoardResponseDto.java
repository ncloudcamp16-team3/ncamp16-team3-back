package tf.tailfriend.board.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tf.tailfriend.board.entity.Board;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardResponseDto {

    private Integer id;
    private Integer boardTypeId;
    private String title;
    private String content;
    private String authorNickname;
    private LocalDateTime createdAt;
    private Integer likeCount;
    private Integer commentCount;
    private List<String> imageUrls;

    @Builder.Default
    private List<CommentResponseDto> comments = new ArrayList<>();

    public static BoardResponseDto fromEntity(Board board) {
        return BoardResponseDto.builder()
                .id(board.getId())
                .boardTypeId(board.getBoardType().getId())
                .title(board.getTitle())
                .content(board.getContent())
                .authorNickname(board.getUser().getNickname())
                .createdAt(board.getCreatedAt())
                .likeCount(board.getLikeCount())
                .commentCount(board.getCommentCount())
                .imageUrls(board.getPhotos().stream()
                        .map(photo -> photo.getFile().getPath())
                        .collect(Collectors.toList()))
                .build();
    }

    public static BoardResponseDto fromEntityWithComments(Board board, List<CommentResponseDto> comments) {
        return BoardResponseDto.builder()
                .id(board.getId())
                .boardTypeId(board.getBoardType().getId())
                .title(board.getTitle())
                .content(board.getContent())
                .authorNickname(board.getUser().getNickname())
                .createdAt(board.getCreatedAt())
                .likeCount(board.getLikeCount())
                .commentCount(board.getCommentCount())
                .imageUrls(board.getPhotos().stream()
                        .map(photo -> photo.getFile().getPath())
                        .collect(Collectors.toList()))
                .comments(comments)
                .build();
    }
}
