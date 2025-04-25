package tf.tailfriend.global.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dongs")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    private Double latitude;

    private Double longitude;
}