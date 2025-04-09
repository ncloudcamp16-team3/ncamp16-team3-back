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

    private String name;
    private String gender;
    private String birth;
    private Double weight;
    private String info;
    private Boolean neutured;

    @Enumerated(EnumType.STRING)
    private ActivityStatus activityStatus;

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
