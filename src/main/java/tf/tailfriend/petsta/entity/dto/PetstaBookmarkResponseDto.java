package tf.tailfriend.petsta.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tf.tailfriend.file.entity.File;
import tf.tailfriend.petsta.entity.PetstaBookmark;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetstaBookmarkResponseDto {
    private Integer id;
    private Integer postId;
    private String userName;
    private String userPhoto;
    private String fileName;
    private File.FileType fileType;
    private Integer likes;
    private Integer comments;
    private String content;
    private String createdAt;

    public static PetstaBookmarkResponseDto fromBookmark(PetstaBookmark bookmark, String fileUrl, String userPhotoUrl) {
        return PetstaBookmarkResponseDto.builder()
                .id(bookmark.getPetstaPost().getId())
                .postId(bookmark.getPetstaPost().getId())
                .userName(bookmark.getPetstaPost().getUser().getNickname())
                .userPhoto(userPhotoUrl)
                .fileName(fileUrl)
                .fileType(bookmark.getPetstaPost().getFile().getType())
                .likes(bookmark.getPetstaPost().getLikeCount())
                .comments(bookmark.getPetstaPost().getCommentCount())
                .content(bookmark.getPetstaPost().getContent())
                .createdAt(bookmark.getPetstaPost().getCreatedAt().toString())
                .build();
    }
}