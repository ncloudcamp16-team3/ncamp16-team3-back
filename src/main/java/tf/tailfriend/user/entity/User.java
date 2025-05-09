package tf.tailfriend.user.entity;

import jakarta.persistence.*;
import lombok.*;
import tf.tailfriend.file.entity.File;
import tf.tailfriend.pet.entity.Pet;
import tf.tailfriend.user.distance.Distance;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder(toBuilder = true)
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

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    private Double latitude;

    private Double longitude;

    @Enumerated(EnumType.STRING)
    private Distance distance;

    @Column(name = "post_count", nullable = false)
    @Builder.Default
    private Integer postCount = 0;

    @Column(name = "follower_count", nullable = false)
    @Builder.Default
    private Integer followerCount = 0;

    @Column(name = "follow_count", nullable = false)
    @Builder.Default
    private Integer followCount = 0;


    @OneToMany(mappedBy = "follower")
    @Builder.Default
    private Set<UserFollow> following = new HashSet<>();

    @OneToMany(mappedBy = "followed")
    @Builder.Default
    private Set<UserFollow> followers = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Pet> pet = new ArrayList<>();

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

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateProfileImage(File file) {
        this.file = file;
    }


    public void setFollowerCount(Integer followerCount) {
        this.followerCount = followerCount;
    }

    public void setFollowCount(Integer followCount) {
        this.followCount = followCount;
    }

    public void markAsDeleted() {
        this.deleted = true;
    }



    public void anonymizeUserData() {
        this.nickname = "탈퇴한 회원입니다";
        this.snsAccountId = "deleted_" + System.currentTimeMillis();
        this.address = null;
        this.dongName = null;
        this.latitude = null;
        this.longitude = null;
        this.distance = null;
        this.deleted = true;
    }
}
