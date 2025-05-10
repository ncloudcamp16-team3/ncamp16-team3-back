package tf.tailfriend.board.dto;

import lombok.*;
import tf.tailfriend.board.entity.Comment;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    private CommentSummaryDto refComment;


    public static CommentResponseDto fromEntity(Comment comment) {
        return CommentResponseDto.builder()
                .id(comment.getId())
                .authorId(comment.getUser().getId())
                .authorNickname(comment.getUser().getNickname())
                .authorProfileImg(comment.getUser().getFile().getPath())
                .content(comment.isDeleted() ? "삭제된 댓글입니다" : comment.getContent())
                .createdAt(comment.getCreatedAt())
                .modified(comment.isModified())
                .deleted(comment.isDeleted())
                .hasParent(comment.hasParent())
                .children(comment.getChildren().stream()
                        .map(CommentResponseDto::fromEntity)
                        .toList())
                .refComment(comment.getRefComment() != null ? CommentSummaryDto.fromEntity(comment.getRefComment()) : null)
                .build();
    }

    @Getter
    public static class CommentSummaryDto {
        private Integer id;
        private String authorNickname;
        private Integer authorId;
        private Boolean deletedAuthor;

        @Builder
        public CommentSummaryDto(Integer id, String authorNickname, Integer authorId, Boolean deletedAuthor) {
            this.id = id;
            this.authorNickname = authorNickname;
            this.authorId = authorId;
            this.deletedAuthor = deletedAuthor;
        }

        public static CommentSummaryDto fromEntity(Comment refcomment) {
            return CommentSummaryDto.builder()
                    .id(refcomment.getId())
                    .authorNickname(refcomment.getUser().getNickname())
                    .authorId(refcomment.getUser().getId())
                    .deletedAuthor(refcomment.getUser().getDeleted())
                    .build();
        }
    }

}
