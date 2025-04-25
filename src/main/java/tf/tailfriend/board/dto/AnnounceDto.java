package tf.tailfriend.board.dto;

import lombok.*;
import tf.tailfriend.admin.entity.Announce;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnounceDto {
    private Integer id;
    private String title;
    private String content;
    private List<String> photos;
    private LocalDateTime createdAt;
    private Integer boardTypeId;

    public static AnnounceDto fromEntity(Announce announce){
        return AnnounceDto.builder()
                .id(announce.getId())
                .title(announce.getTitle())
                .content(announce.getContent())
                .photos(announce.getPhotos().stream()
                        .map(photo -> photo.getFile().getPath())
                        .toList())
                .createdAt(announce.getCreatedAt())
                .boardTypeId(announce.getBoardType().getId())
                .build();
    }
}
