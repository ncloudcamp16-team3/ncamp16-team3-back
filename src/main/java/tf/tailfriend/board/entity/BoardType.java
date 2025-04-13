package tf.tailfriend.board.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "board_types")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String name;
}
