package tf.tailfriend.pet.entity;

import jakarta.persistence.*;
import lombok.*;
import tf.tailfriend.file.entity.File;

import java.io.Serializable;

@Entity
@Table(name = "pet_photos")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetPhoto {

    @EmbeddedId
    private PetPhotoId id;

    @MapsId("petId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id")
    private Pet pet;

    @MapsId("fileId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private File file;

    @Column(nullable = false)
    private boolean thumbnail = false;

    @Embeddable
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PetPhotoId implements Serializable {

        @Column(name = "pet_id")
        private Integer petId;

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
            PetPhotoId that = (PetPhotoId) obj;
            return fileId.equals(that.fileId) && petId.equals(that.petId);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(fileId, petId);
        }
    }

    public static PetPhoto of(Pet pet, File file, boolean thumbnail) {
        PetPhotoId id = PetPhotoId.builder()
                .petId(pet.getId())
                .fileId(file.getId())
                .build();

        return PetPhoto.builder()
                .id(id)
                .pet(pet)
                .file(file)
                .thumbnail(thumbnail)
                .build();
    }
    public void setThumbnail(boolean thumbnail) {
        this.thumbnail = thumbnail;
    }
}
