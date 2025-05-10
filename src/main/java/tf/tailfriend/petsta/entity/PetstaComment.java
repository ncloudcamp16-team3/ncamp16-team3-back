package tf.tailfriend.petsta.entity;

import jakarta.persistence.*;
import lombok.*;
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

    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean deleted; // 🔥 삭제 여부

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private PetstaComment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.PERSIST)
    private List<PetstaComment> replies = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    @Column(name = "reply_count", nullable = false)
    private Integer replyCount = 0;

    @OneToOne(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    private PetstaCommentMention mention;

    // 🔽 멘션 설정
    public void setMention(PetstaCommentMention mention) {
        this.mention = mention;
        if (mention != null) {
            mention.setComment(this);
        }
    }

    // 🔽 대댓글 추가
    public void addReply(PetstaComment reply) {
        replies.add(reply);
        reply.parent = this;
        replyCount++;
    }

    // 🔽 댓글 삭제 처리 (소프트 삭제)
    public void markAsDeleted() {
        this.content = "";
        this.deleted = true;
    }

    public void setReplyCount(int replyCount) {
        this.replyCount = replyCount;
    }

    public void clearMention() {
        if (this.mention != null) {
            this.mention.setComment(null); // 관계 끊기
            this.mention = null;
        }
    }

    public void setContent(String replacedContent) {
        this.content = replacedContent;
    }
}
