package tf.tailfriend.pet.entity;

import jakarta.persistence.*;
import lombok.*;
import tf.tailfriend.user.entity.User;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pets")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_type_id", nullable = false)
    private PetType petType;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String gender;

    @Column(nullable = false, length = 50)
    private String birth;

    @Column(nullable = false)
    private Double weight;

    @Column(nullable = false)
    private String info;

    @Column(nullable = false)
    private Boolean neutered;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_status", nullable = false)
    private ActivityStatus activityStatus = ActivityStatus.NONE;

    public enum ActivityStatus {
        WALK, PLAY, NONE
    }

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PetPhoto> photos = new ArrayList<>();

    public void addPhoto(PetPhoto photo) {
        photos.add(photo);
        photo.setPet(this);
    }

    public void removePhoto(PetPhoto photo) {
        photos.remove(photo);
        photo.setPet(null);
    }
}
