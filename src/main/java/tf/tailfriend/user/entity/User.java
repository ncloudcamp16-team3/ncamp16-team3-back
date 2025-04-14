package tf.tailfriend.user.entity;

import jakarta.persistence.*;
import lombok.*;
import tf.tailfriend.file.entity.File;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    @Builder.Default
    private Set<UserFollow> following = new HashSet<>();

    @OneToMany(mappedBy = "followed")
    @Builder.Default
    private Set<UserFollow> followers = new HashSet<>();

    public void follow(User userToFollow) {
        UserFollow.UserFollowId id = UserFollow.UserFollowId.builder()
                .followerId(this.id)
                .followedId(userToFollow.getId())
                .build();

        UserFollow follow = UserFollow.builder()
                .id(id)
                .follower(this)
                .followed(userToFollow)
                .build();

        this.following.add(follow);
        userToFollow.getFollowers().add(follow);
    }

    public void unFollow(User userToFollow) {
        UserFollow follow = UserFollow.of(this, userToFollow);
        this.following.add(follow);
        userToFollow.getFollowers().add(follow);
    }
}
