package tf.tailfriend.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import tf.tailfriend.board.entity.Board;
import tf.tailfriend.user.entity.User;

@Entity
@Table(name = "trade_matches")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ✅ 여기도 Integer

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "post_id", nullable = false)
    private Integer postId;

    public static TradeMatch of(User user, Integer postId) {
        return TradeMatch.builder()
                .user(user)
                .postId(postId)
                .build();
    }
}
