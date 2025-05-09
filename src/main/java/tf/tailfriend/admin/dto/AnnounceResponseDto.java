package tf.tailfriend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tf.tailfriend.admin.entity.Announce;
import tf.tailfriend.admin.entity.AnnouncePhoto;
import tf.tailfriend.global.service.StorageService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnounceResponseDto {

    private Integer id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private Integer boardTypeId;
    private String boardTypeName;
    private List<AnnouncePhotoDto> photos;

    public static AnnounceResponseDto fromEntity(Announce announce, StorageService storageService) {
        List<AnnounceResponseDto.AnnouncePhotoDto> photoDtos = announce.getPhotos().stream()
                .map(photo -> AnnounceResponseDto.AnnouncePhotoDto.fromEntity(photo, storageService))
                .collect(Collectors.toList());
        return AnnounceResponseDto.builder()
                .id(announce.getId())
                .title(announce.getTitle())
                .content(announce.getContent())
                .createdAt(announce.getCreatedAt())
                .boardTypeId(announce.getBoardType().getId())
                .boardTypeName(announce.getBoardType().getName())
                .photos(photoDtos)
                .build();
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnnouncePhotoDto {

        private Integer fileId;
        private String url;

        public static AnnouncePhotoDto fromEntity(AnnouncePhoto photo, StorageService storageService) {
            return AnnouncePhotoDto.builder()
                    .fileId(photo.getFile().getId())
                    .url(storageService.generatePresignedUrl(photo.getFile().getPath()))
                    .build();
        }
    }
}


