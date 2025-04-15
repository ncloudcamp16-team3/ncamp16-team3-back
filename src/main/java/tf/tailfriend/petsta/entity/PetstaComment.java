package tf.tailfriend.petsta.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import tf.tailfriend.user.entity.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "petsta_comments")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetstaComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private PetstaPost post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private PetstaComment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.PERSIST)
    private List<PetstaComment> replies = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "reply_count", nullable = false)
    private Integer replyCount = 0;

    public void addReply(PetstaComment reply) {
        replies.add(reply);
        reply.parent = this;
        replyCount++;
    }
}
