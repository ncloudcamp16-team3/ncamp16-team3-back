package tf.tailfriend.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_fcm")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFcm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "fcm_token", columnDefinition = "TEXT", nullable = false)
    private String fcmToken;

}