package tf.tailfriend.facility.entity;

import jakarta.persistence.*;
import lombok.*;
import tf.tailfriend.file.entity.File;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "facility_photos")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityPhoto {

    @EmbeddedId
    private FacilityPhotoId id;

    @MapsId("fileId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private File file;

    @MapsId("facilityId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id")
    private Facility facility;

    @Embeddable
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacilityPhotoId implements Serializable {

        @Column(name = "file_id")
        private Integer fileId;

        @Column(name = "facility_id")
        private Integer facilityId;

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            FacilityPhotoId that = (FacilityPhotoId) obj;
            return fileId.equals(that.fileId) &&
                    facilityId.equals(that.facilityId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fileId, facilityId);
        }
    }

    public static FacilityPhoto of(File file, Facility facility) {
        FacilityPhotoId id = FacilityPhotoId.builder()
                .facilityId(facility.getId())
                .fileId(file.getId())
                .build();

        return FacilityPhoto.builder()
                .id(id)
                .facility(facility)
                .file(file)
                .build();
    }
}
