package tf.tailfriend.board.dto;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import tf.tailfriend.board.entity.Comment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CommentResponseDto {

    private Integer id;
    private Integer authorId;
    private String authorNickname;
    private String authorProfileImg;
    private String content;
    private LocalDateTime createdAt;
    private boolean modified;
    private boolean deleted;
    private boolean hasParent;
    private List<CommentResponseDto> children;
    private CommentResponseDto refComment;


    public static CommentResponseDto fromEntity(Comment comment) {
        return CommentResponseDto.builder()
                .id(comment.getId())
                .authorId(comment.getUser().getId())
                .authorNickname(comment.getUser().getNickname())
                .authorProfileImg(comment.getUser().getFile().getPath())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .modified(comment.isModified())
                .deleted(comment.isDeleted())
                .hasParent(comment.hasParent())
                .children(comment.getChildren().stream()
                        .map(CommentResponseDto::fromEntity)
                        .toList())
                .refComment(comment.getRefComment() != null ? CommentResponseDto.fromEntity(comment.getRefComment()) : null)

                .build();
    }
}
