package tf.tailfriend.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tf.tailfriend.user.entity.UserFollow;

import java.util.Optional;

public interface UserFollowDao extends JpaRepository<UserFollow, Integer> {
    Optional<UserFollow> findByFollowerIdAndFollowedId(Integer followerId, Integer followedId);
    boolean existsByFollowerIdAndFollowedId(Integer followerId, Integer followedId);

    @Modifying
    @Query("DELETE FROM UserFollow uf WHERE uf.follower.id = :followerId")
    void deleteByFollowerId(@Param("followerId") Integer followerId);

    @Modifying
    @Query("DELETE FROM UserFollow uf WHERE uf.followed.id = :followedId")
    void deleteByFollowedId(@Param("followedId") Integer followedId);
}
