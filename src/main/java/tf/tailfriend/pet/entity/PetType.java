package tf.tailfriend.pet.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pet_types")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, length = 50)
    private String name;
}
