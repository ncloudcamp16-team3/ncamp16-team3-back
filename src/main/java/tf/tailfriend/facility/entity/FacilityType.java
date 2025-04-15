package tf.tailfriend.facility.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "facility_types")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String name;
}
