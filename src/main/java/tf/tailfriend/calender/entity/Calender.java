package tf.tailfriend.calender.entity;

import jakarta.persistence.*;
import lombok.*;
import tf.tailfriend.user.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "calender_schedules")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Calender {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String title;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;
}
