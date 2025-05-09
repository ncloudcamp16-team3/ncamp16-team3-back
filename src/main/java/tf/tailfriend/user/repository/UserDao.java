package tf.tailfriend.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tf.tailfriend.user.entity.User;

import java.util.Optional;

public interface UserDao extends JpaRepository<User, Integer> {

    Optional<User> findByIdAndDeletedFalse(Integer id);
    Optional<User> findBySnsAccountIdAndSnsTypeIdAndDeletedFalse(String snsAccountId, Integer snsTypeId);
    Optional<User> findByNicknameAndDeletedFalse(String nickname);

    boolean existsByNicknameAndDeletedFalse(String nickname);

    @Modifying
    @Query("update User u set u.postCount = u.postCount + 1 where u.id = :id")
    void incrementPostCount(@Param("id") Integer userId);

    @Modifying
    @Query("update User u set u.postCount = u.postCount - 1 where u.id = :id")
    void decrementPostCount(@Param("id") Integer userId);

    @Modifying
    @Query("update User u set u.followerCount = u.followerCount + 1 where u.id = :id")
    void incrementFollowerCount(@Param("id") Integer userId);

    @Modifying
    @Query("update User u set u.followerCount = u.followerCount - 1 where u.id = :id")
    void decrementFollowerCount(@Param("id") Integer userId);

    @Modifying
    @Query("update User u set u.followCount = u.followCount + 1 where u.id = :id")
    void incrementFollowCount(@Param("id") Integer userId);

    @Modifying
    @Query("update User u set u.followCount = u.followCount - 1 where u.id = :id")
    void decrementFollowCount(@Param("id") Integer userId);

    boolean existsByNickname(String nickname);

}
