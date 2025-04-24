package tf.tailfriend.user.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tf.tailfriend.user.entity.User;
import tf.tailfriend.user.entity.UserFollow;

import java.util.List;
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

    // UserFollowDao.java
    @Query("SELECT uf.followed FROM UserFollow uf WHERE uf.follower.id = :followerId")
    List<User> findTop10ByFollowerId(@Param("followerId") Integer followerId, Pageable pageable);

}
