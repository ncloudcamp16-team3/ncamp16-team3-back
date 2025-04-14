package tf.tailfriend.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sns_types")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SnsType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String name;
}
