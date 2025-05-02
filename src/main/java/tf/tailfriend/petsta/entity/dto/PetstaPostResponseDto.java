package tf.tailfriend.petsta.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tf.tailfriend.file.entity.File;
import tf.tailfriend.petsta.entity.PetstaPost;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PetstaPostResponseDto {
    private Integer postId;
    private Integer userId;
    private String userName;
    private String userPhoto;
    private String fileName;
    private File.FileType fileType;
    private Integer likes;
    private Integer comments;
    private String content;
    private String createdAt;
    private Boolean initialLiked;
    private Boolean initialBookmarked;
    private Boolean initialFollowed;
    private Boolean isVisited;

    public PetstaPostResponseDto(PetstaPost post, boolean initialLiked, boolean initialBookmarked, boolean initialFollowed, boolean isVisited) {
        this.postId = post.getId();  // 게시물 ID
        this.userId = post.getUser().getId();  // User ID
        this.userName = post.getUser().getNickname();  // User의 nickname
        this.userPhoto = post.getUser().getFile().getPath();  // User의 profile 사진
        this.fileName = post.getFile().getPath();  // 파일 이름
        this.fileType = post.getFile().getType();  // 파일 타입
        this.likes = post.getLikeCount();  // 좋아요 개수
        this.comments = post.getCommentCount();  // 댓글 개수
        this.content = post.getContent();  // 게시물 내용
        this.createdAt = post.getCreatedAt()
                .atZone(ZoneId.of("Asia/Seoul"))
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        this.initialLiked = initialLiked;
        this.initialBookmarked = initialBookmarked;
        this.initialFollowed = initialFollowed;
        this.isVisited = isVisited;
    }

}
