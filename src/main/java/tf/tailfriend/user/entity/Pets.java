package tf.tailfriend.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "pets")
public class Pets {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private Users user;

    @ManyToOne
    @JoinColumn(name = "pet_type_id", nullable = false)
    private PetTypes petType;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 50, nullable = false)
    private String gender;

    @Column(length = 50, nullable = false)
    private String birth;

    @Column(nullable = false)
    private Double weight;

    @Column(length = 255, nullable = false)
    private String info;

    @Column(nullable = false)
    private Boolean neutered;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_status", nullable = false)
    private ActivityStatus activityStatus = ActivityStatus.NONE;

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PetPhotos> petPhoto = new ArrayList<>();

    // ✅ 내부 enum 정의 (public으로 설정해서 외부에서도 사용 가능)
    public enum ActivityStatus {
        WALK, PLAY, NONE
    }
}
