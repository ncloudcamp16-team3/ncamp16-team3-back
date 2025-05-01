package tf.tailfriend.board.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tf.tailfriend.board.entity.Board;
import tf.tailfriend.board.entity.BoardBookmark;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardBookmarkResponseDto {
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
    private List<String> imageUrls;
    private Integer price; // 중고거래 게시판용
    private String address; // 중고거래 게시판용
    private Boolean sell; // 중고거래 게시판용

    public static BoardBookmarkResponseDto fromBookmark(BoardBookmark bookmark, List<String> imageUrls) {
        Board board = bookmark.getBoard();

        BoardBookmarkResponseDto dto = BoardBookmarkResponseDto.builder()
                .id(board.getId())
                .boardTypeId(board.getBoardType().getId())
                .title(board.getTitle())
                .content(board.getContent())
                .authorNickname(board.getUser().getNickname())
                .authorId(board.getUser().getId())
                .createdAt(board.getCreatedAt())
                .likeCount(board.getLikeCount())
                .commentCount(board.getCommentCount())
                .imageUrls(imageUrls)
                .build();

        if(!imageUrls.isEmpty()) {
            dto.firstImageUrl = imageUrls.get(0);
        }

        // 중고거래 게시판인 경우 Product 정보 설정
        board.getProduct().ifPresent(product -> {
            dto.price = product.getPrice();
            dto.address = product.getAddress();
            dto.sell = product.getSell();
        });

        return dto;
    }
}