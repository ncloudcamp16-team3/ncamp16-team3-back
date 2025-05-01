package tf.tailfriend.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.chat.entity.ChatRoom;
import tf.tailfriend.chat.entity.TradeMatch;

public interface TradeMatchDao extends JpaRepository<TradeMatch, Integer> {
    boolean existsByUserIdAndPostId(int i, int i1);
}
