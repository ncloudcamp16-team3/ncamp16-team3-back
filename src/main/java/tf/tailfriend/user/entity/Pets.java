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
    private Users owner;

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
    private Boolean neutured;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_status", nullable = false)
    private ActivityStatus activityStatus = ActivityStatus.NONE;  // 기본값 NONE으로 설정

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PetPhoto> petPhotos = new ArrayList<>();

    // ✅ 양방향 연관관계 편의 메서드
    public void addPhoto(PetPhoto photo) {
        petPhotos.add(photo);
        photo.setPet(this);
    }

    // ✅ 리스트 통째로 설정 시도할 때도 양방향 연관관계 유지
    public void setPhotos(List<PetPhoto> photos) {
        this.petPhotos.clear(); // 기존 리스트 초기화
        if (photos != null) {
            for (PetPhoto photo : photos) {
                addPhoto(photo);
            }
        }
    }

    // ✅ 내부 enum 정의 (public으로 설정해서 외부에서도 사용 가능)
    public enum ActivityStatus {
        WALK, PLAY, NONE
    }
}
