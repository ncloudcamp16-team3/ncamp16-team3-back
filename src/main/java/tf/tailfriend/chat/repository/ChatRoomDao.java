package tf.tailfriend.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.chat.entity.ChatRoom;
import tf.tailfriend.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface ChatRoomDao extends JpaRepository<ChatRoom, Integer> {

    Optional<ChatRoom> findByUser1AndUser2(User user1, User user2);

    List<ChatRoom> findAllByUser1OrUser2(User user1, User user2);


    List<ChatRoom> findByUniqueIdIn(List<String> uniqueIds);

    Optional<ChatRoom> findByUniqueId(String channelId);


}
