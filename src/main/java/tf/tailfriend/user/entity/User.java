package tf.tailfriend.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tf.tailfriend.file.entity.File;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false, length = 50)
    private String nickname;

    @Column(name = "sns_account_id", nullable = false)
    private String snsAccountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sns_type_id" , nullable = false)
    private SnsType snsType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private File file;

    private String address;

    @Column(name = "dong_name")
    private String dongName;

    private Double latitude;

    private Double longitude;

    @Enumerated(EnumType.STRING)
    private Distance distance;

    @Getter
    public enum Distance {
        ONE("1"), TWO("2"), THREE("3"), FOUR("4");

        private final String value;

        Distance(String value) {
            this.value = value;
        }
    }

    @OneToMany(mappedBy = "follower")
    private Set<UserFollow> following = new HashSet<>();

    @OneToMany(mappedBy = "followed")
    private Set<UserFollow> followers = new HashSet<>();

    public void follow(User userToFollow) {
        UserFollow follow = new UserFollow();
        UserFollow.UserFollowId id = new UserFollow.UserFollowId();
        id.setFollowedId(this.id);
        id.setFollowerId(userToFollow.getId());
        follow.setId(id);
        follow.setFollower(this);
        follow.setFollowed(userToFollow);

        this.following.add(follow);
        userToFollow.getFollowers().add(follow);
    }

    public void unFollow(User userToUnFollow) {
        this.following.removeIf(follow -> follow.getFollowed().equals(userToUnFollow));
        userToUnFollow.getFollowers().removeIf(follow -> follow.getFollowed().equals(this));
    }
}
