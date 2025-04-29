package tf.tailfriend.board.dto;

import lombok.*;
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
    private Integer authorId;
    private LocalDateTime createdAt;
    private Integer likeCount;
    private Integer commentCount;

    private String firstImageUrl;
    private List<String> imageUrls = new ArrayList<>();

    @Builder.Default
    private List<CommentResponseDto> comments = new ArrayList<>();

    // 첫 번째 이미지 URL 업데이트 메서드
    public void updateFirstImageUrl(String url) {
        this.firstImageUrl = url;

        // 이미지 URL 목록이 비어있으면 첫 번째 이미지도 추가
        if (this.imageUrls.isEmpty() && url != null) {
            this.imageUrls.add(url);
        }
    }

    // 이미지 URL 목록 설정 메서드
    public void setImageUrls(List<String> urls) {
        this.imageUrls = urls;

        // 첫 번째 이미지 URL도 업데이트
        if (!urls.isEmpty()) {
            this.firstImageUrl = urls.get(0);
        }
    }

    public static BoardResponseDto fromEntity(Board board) {
        return BoardResponseDto.builder()
                .id(board.getId())
                .boardTypeId(board.getBoardType().getId())
                .title(board.getTitle())
                .content(board.getContent())
                .authorNickname(board.getUser().getNickname())
                .authorId(board.getUser().getId())
                .createdAt(board.getCreatedAt())
                .likeCount(board.getLikeCount())
                .commentCount(board.getCommentCount())
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

    public static class AuthorDto {
        private Integer id;
        private String nickname;
    }
}
