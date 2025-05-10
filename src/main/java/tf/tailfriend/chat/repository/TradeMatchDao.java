package tf.tailfriend.chat.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tf.tailfriend.chat.entity.TradeMatch;

public interface TradeMatchDao extends JpaRepository<TradeMatch, Integer> {
    boolean existsByUserIdAndPostId(int i, int i1);

    @Modifying
    @Query("DELETE FROM TradeMatch t WHERE t.postId = :postId")
    void deleteAllByPostId(@Param("postId") Integer postId);

    @Modifying
    @Query("DELETE FROM TradeMatch t WHERE t.user.id = :userId")
    void deleteByUserId(@Param("userId") Integer userId);

    Iterable<? extends TradeMatch> findAllByUserId(Integer userId);

    @Transactional
    @Modifying
    @Query("DELETE FROM TradeMatch tm WHERE tm.postId = :postId")
    void deleteByPostId(@Param("postId") Integer postId);
}
