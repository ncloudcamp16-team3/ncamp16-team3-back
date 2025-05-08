package tf.tailfriend.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tf.tailfriend.chat.entity.ChatRoom;
import tf.tailfriend.pet.entity.PetMatch;
import tf.tailfriend.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface ChatRoomDao extends JpaRepository<ChatRoom, Integer> {

    Optional<ChatRoom> findByUser1AndUser2(User user1, User user2);

    List<ChatRoom> findAllByUser1OrUser2(User user1, User user2);


    List<ChatRoom> findByUniqueIdIn(List<String> uniqueIds);

    Optional<ChatRoom> findByUniqueId(String channelId);


    @Query("SELECT c FROM ChatRoom c WHERE c.user1.id = :userId OR c.user2.id = :userId")
    List<ChatRoom> findAllByUserId(@Param("userId") Integer userId);
}
