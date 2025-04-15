package tf.tailfriend.notification.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification_types")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String name;
}
