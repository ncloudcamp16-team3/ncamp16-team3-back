package tf.tailfriend.sitter.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import tf.tailfriend.file.entity.File;
import tf.tailfriend.pet.entity.PetType;
import tf.tailfriend.user.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "pet_sitters")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sitter {

    @Id
    @Column(name = "id")
    private Integer id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_type_id")
    private PetType petType;

    @Column(nullable = false, length = 50)
    private String age;

    @Column(name = "house_type", nullable = false, length = 50)
    private String houseType;

    private String comment;

    @Column(nullable = false)
    private Boolean grown = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "pet_count")
    private PetCount petCount;

    @Column(name = "sitter_exp", nullable = false)
    private Boolean sitterExp = false;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private File file;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "apply_at")
    private LocalDateTime applyAt;

    @Getter
    public enum PetCount {
        ONE("1"), TWO("2"), THREE_PLUS("3+");

        private final String value;

        PetCount(String value) {
            this.value = value;
        }
    }
}
