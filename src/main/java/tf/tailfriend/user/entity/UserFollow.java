package tf.tailfriend.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "user_follows")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserFollowId implements Serializable {


        @Column(name = "follower_id")
        private Integer followerId;

        @Column(name = "followed_id")
        private Integer followedId;

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

    public static UserFollow of(User follower, User followed) {
        UserFollowId id = UserFollowId.builder()
                .followerId(follower.getId())
                .followedId(followed.getId())
                .build();

        return UserFollow.builder()
                .id(id)
                .follower(follower)
                .followed(followed)
                .build();
    }
}
