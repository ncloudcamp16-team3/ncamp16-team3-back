package tf.tailfriend.petsta.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import tf.tailfriend.file.entity.File;
import tf.tailfriend.user.entity.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "petsta_posts")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetstaPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private File file;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thumbnail_file_id")
    private File thumbnailFile;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean deleted; // 🔥 삭제 여부

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    @Builder.Default
    @Column(name = "comment_count", nullable = false)
    private Integer commentCount = 0;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PetstaComment> comments = new ArrayList<>();


    // 🔽 댓글 삭제 처리 (소프트 삭제)
    public void markAsDeleted() {
        this.content = "";
        this.deleted = true;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void decreaseCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }

}
