package tf.tailfriend.petsta.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tf.tailfriend.user.entity.User;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "petsta_bookmarks")
@Getter
@Setter
@NoArgsConstructor
public class PetstaBookmark {

    @EmbeddedId
    private PetstaBookmarkId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @MapsId("petstaPostId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "petsta_post_id", nullable = false)
    private PetstaPost petstaPost;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    public static class PetstaBookmarkId implements Serializable {

        @Column(name = "user_id")
        private Integer userId;

        @Column(name = "petsta_post_id")
        private Integer petstaPostId;

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            PetstaBookmarkId that = (PetstaBookmarkId) obj;
            return userId.equals(that.userId) && petstaPostId.equals(that.petstaPostId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, petstaPostId);
        }
    }
}
