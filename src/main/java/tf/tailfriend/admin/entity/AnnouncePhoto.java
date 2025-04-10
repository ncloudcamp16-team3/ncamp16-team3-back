package tf.tailfriend.admin.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tf.tailfriend.file.entity.File;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "announce_photos")
@Getter
@Setter
@NoArgsConstructor
public class AnnouncePhoto {

    @EmbeddedId
    private AnnouncePhotoId id;

    @MapsId("announceId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "announce_id")
    private Announce announce;

    @MapsId("fileId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private File file;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    public static class AnnouncePhotoId implements Serializable {

        @Column(name = "announce_id")
        private Integer announceId;

        @Column(name = "file_id")
        private Integer fileId;

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            AnnouncePhotoId other = (AnnouncePhotoId) obj;
            return announceId.equals(other.announceId) && fileId.equals(other.fileId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(announceId, fileId);
        }
    }
}
