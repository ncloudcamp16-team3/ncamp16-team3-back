package tf.tailfriend.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pet_photos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetPhoto {

    @EmbeddedId
    private PetPhotoId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("fileId")
    @JoinColumn(name = "file_id")
    private Files file;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("petId")
    @JoinColumn(name = "pet_id")
    private Pets pet;

    @Column(nullable = false)
    private boolean thumbnail = false;
}
