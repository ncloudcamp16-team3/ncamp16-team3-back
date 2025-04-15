package tf.tailfriend.user.entity;//package tf.tailfriend.user.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//@Entity
//@Getter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//@Table(name = "pet_photos")
//public class PetPhotos {
//
//    @EmbeddedId
//    private PetPhotosId id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @MapsId("fileId")
//    @JoinColumn(name = "file_id")
//    private Files file;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @MapsId("petId")
//    @JoinColumn(name = "pet_id")
//    private Pets pet;
//
//    @Column(nullable = false)
//    private boolean thumbnail = false;
//}
