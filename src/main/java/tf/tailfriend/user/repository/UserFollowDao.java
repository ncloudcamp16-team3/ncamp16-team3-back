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



    // 내가 팔로우한 사람들
    @Query("SELECT uf FROM UserFollow uf JOIN FETCH uf.followed WHERE uf.follower.id = :userId")
    List<UserFollow> findAllByFollowerId(@Param("userId") Integer userId);

    // 나를 팔로우한 사람들
    @Query("SELECT uf FROM UserFollow uf JOIN FETCH uf.follower WHERE uf.followed.id = :userId")
    List<UserFollow> findAllByFollowedId(@Param("userId") Integer userId);

    // UserFollowDao.java
    @Query("SELECT uf.followed FROM UserFollow uf WHERE uf.follower.id = :followerId")
    List<User> findTop10ByFollowerId(@Param("followerId") Integer followerId, Pageable pageable);



    List<UserFollow> findByFollowerIdAndFollowedIdIn(Integer currentUserId, List<Integer> collect);

    List<UserFollow> findByFollowedId(Integer followedId, Pageable pageable);
    List<UserFollow> findByFollowerId(Integer followerId, Pageable pageable);

}
