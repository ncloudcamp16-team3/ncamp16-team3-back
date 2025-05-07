package tf.tailfriend.petsta.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import tf.tailfriend.user.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "petsta_comment_mentions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetstaCommentMention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private PetstaComment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentioned_user_id", nullable = false)
    private User mentionedUser;

    @Column(name = "mentioned_nickname", nullable = false)
    private String mentionedNickname;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
