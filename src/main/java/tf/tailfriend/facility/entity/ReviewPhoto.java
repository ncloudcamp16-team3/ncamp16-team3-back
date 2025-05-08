package tf.tailfriend.facility.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tf.tailfriend.file.entity.File;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "review_photos")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewPhoto {

    @EmbeddedId
    private ReviewPhotoId id;

    @MapsId("fileId")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private File file;

    @MapsId("reviewId")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

    @Embeddable
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewPhotoId implements Serializable {

        @Column(name = "file_id")
        private Integer fileId;

        @Column(name = "review_id")
        private Integer reviewId;

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            ReviewPhotoId that = (ReviewPhotoId) obj;
            return fileId.equals(that.fileId) &&
                    reviewId.equals(that.reviewId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fileId, reviewId);
        }
    }

    public static ReviewPhoto of(File file, Review review) {
        ReviewPhotoId id = ReviewPhotoId.builder()
                .reviewId(review.getId())
                .fileId(file.getId())
                .build();

        return ReviewPhoto.builder()
                .id(id)
                .file(file)
                .review(review)
                .build();
    }
}
