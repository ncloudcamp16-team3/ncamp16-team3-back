package tf.tailfriend.board.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import tf.tailfriend.board.entity.Board;
import tf.tailfriend.board.entity.Product;
import tf.tailfriend.file.entity.File;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BoardResponseDto {

    private Integer id;
    private Integer boardTypeId;
    private String title;
    private String content;
    private String authorNickname;
    private Integer authorId;
    private String authorAddress;
    private String authorProfileImg;
    private LocalDateTime createdAt;
    private Integer likeCount;
    private Integer commentCount;
    private Integer price;
    private Boolean sell;
    private String address;

    private String firstImageUrl;
    private List<String> imageUrls = new ArrayList<>();
    private List<PhotoDto> photos = new ArrayList<>();

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
                .authorAddress(board.getUser().getAddress())
                .authorProfileImg(board.getUser().getFile().getPath())
                .imageUrls(board.getPhotos().stream()
                        .map(photo -> photo.getFile().getPath())
                        .collect(Collectors.toList()))
                .photos(board.getPhotos().stream()
                        .map(photo -> new PhotoDto(photo.getFile()))
                        .collect(Collectors.toList()))
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
                .authorId(board.getUser().getId())
                .authorAddress(board.getUser().getAddress())
                .authorProfileImg(board.getUser().getFile().getPath())
                .createdAt(board.getCreatedAt())
                .likeCount(board.getLikeCount())
                .commentCount(board.getCommentCount())
                .imageUrls(board.getPhotos().stream()
                        .map(photo -> photo.getFile().getPath())
                        .collect(Collectors.toList()))
                .photos(board.getPhotos().stream()
                        .map(photo -> new PhotoDto(photo.getFile()))
                        .collect(Collectors.toList()))
                .comments(comments)
                .build();
    }

    public static BoardResponseDto fromProductEntity(Product product) {
        return BoardResponseDto.builder()
                .id(product.getBoard().getId())
                .boardTypeId(product.getBoard().getBoardType().getId())
                .title(product.getBoard().getTitle())
                .content(product.getBoard().getContent())
                .authorNickname(product.getBoard().getUser().getNickname())
                .authorId(product.getBoard().getUser().getId())
                .authorAddress(product.getBoard().getUser().getAddress())
                .authorProfileImg(product.getBoard().getUser().getFile().getPath())
                .createdAt(product.getBoard().getCreatedAt())
                .likeCount(product.getBoard().getLikeCount())
                .commentCount(product.getBoard().getCommentCount())
                .imageUrls(product.getBoard().getPhotos().stream()
                        .map(photo -> photo.getFile().getPath())
                        .collect(Collectors.toList()))
                .photos(product.getBoard().getPhotos().stream()
                        .map(photo -> new PhotoDto(photo.getFile()))
                        .collect(Collectors.toList()))
                .price(product.getPrice())
                .sell(product.getSell())
                .address(product.getAddress())
                .build();
    }

    @ToString
    @Setter
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PhotoDto {
        Integer id;
        String path;

        public PhotoDto(File file){
            this.id = file.getId();
            this.path = file.getPath();
        }
    }
}
