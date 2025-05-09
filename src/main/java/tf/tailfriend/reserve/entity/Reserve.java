package tf.tailfriend.reserve.entity;

import jakarta.persistence.*;
import lombok.*;
import tf.tailfriend.facility.entity.Facility;
import tf.tailfriend.user.entity.User;
import tf.tailfriend.facility.entity.Review;

import java.time.LocalDateTime;

@Entity
@Table(name = "reserves")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reserve {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @Column(name = "entry_time", nullable = false)
    private LocalDateTime entryTime;

    @Column(name = "exit_time")
    private LocalDateTime exitTime;

    @Column(nullable = false)
    private Integer amount;

    @Column(name = "reserve_status", nullable = false)
    private Boolean reserveStatus = false;

    @OneToOne(mappedBy = "reserve", cascade = CascadeType.ALL, orphanRemoval = true)
    private Payment payment;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = true)
    private Review review;

    public void updateReview(Review review) {
        this.review = review;
    }
}
