package tf.tailfriend.pet.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pet_matches",
        uniqueConstraints = @UniqueConstraint(columnNames = {"pet1_id", "pet2_id"}))
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PetMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet1_id", nullable = false)
    private Pet pet1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet2_id", nullable = false)
    private Pet pet2;

    public static PetMatch of(Pet pet1, Pet pet2) {
        return PetMatch.builder()
                .pet1(pet1)
                .pet2(pet2)
                .build();
    }

    public static PetMatch ofOrdered(Pet petA, Pet petB) {
        if (petA.getId() < petB.getId()) {
            return of(petA, petB);
        } else {
            return of(petB, petA);
        }
    }
}
