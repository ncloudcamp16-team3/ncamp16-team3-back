package tf.tailfriend.chat.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chat_types")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String name;
}
