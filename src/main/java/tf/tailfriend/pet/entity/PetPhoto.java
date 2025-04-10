package tf.tailfriend.pet.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tf.tailfriend.file.entity.File;

import java.io.Serializable;

@Entity
@Table(name = "pet_photos")
@Getter
@Setter
@NoArgsConstructor
public class PetPhoto {

    @EmbeddedId
    private PetPhotoId id;

    @MapsId("fileId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private File file;

    @MapsId("petId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id")
    private Pet pet;

    @Column(nullable = false)
    private boolean thumbnail = false;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    public static class PetPhotoId implements Serializable {

        @Column(name = "file_id")
        private Integer fileId;

        @Column(name = "pet_id")
        private Integer petId;

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            PetPhotoId that = (PetPhotoId) obj;
            return fileId.equals(that.fileId) && petId.equals(that.petId);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(fileId, petId);
        }
    }
}
