package tf.tailfriend.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "user_follows")
@Getter
@Setter
@NoArgsConstructor
public class UserFollow {

    @EmbeddedId
    private UserFollowId id;

    @MapsId("followerId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id")
    private User follower;

    @MapsId("followedId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "followed_id")
    private User followed;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    public static class UserFollowId implements Serializable {

        @Column(name = "follower_id")
        private Integer followerId;

        @Column(name = "followed_id")
        private Integer followedId;

        public UserFollowId(Integer followerId, Integer followedId) {
            this.followerId = followerId;
            this.followedId = followedId;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            UserFollowId that = (UserFollowId) obj;
            return followerId.equals(that.followerId) && followedId.equals(that.followedId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(followerId, followedId);
        }
    }
}
