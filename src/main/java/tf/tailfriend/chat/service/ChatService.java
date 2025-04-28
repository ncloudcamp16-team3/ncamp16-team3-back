package tf.tailfriend.chat.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tf.tailfriend.chat.entity.ChatRoom;
import tf.tailfriend.chat.repository.ChatRoomDao;
import tf.tailfriend.user.entity.User;
import tf.tailfriend.user.repository.UserDao;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomDao chatRoomDao;
    private final UserDao userDao;

    @Transactional
    public String createOrGetRoom(Integer currentUserId, Integer targetUserId) {
        User userA = userDao.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음: " + currentUserId));
        User userB = userDao.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음: " + targetUserId));

        // ID 오름차순 정렬
        List<User> sorted = Stream.of(userA, userB)
                .sorted(Comparator.comparingInt(User::getId))
                .collect(Collectors.toList());

        User user1 = sorted.get(0);
        User user2 = sorted.get(1);

        // 이미 존재하는 방 있는지 확인
        Optional<ChatRoom> existing = chatRoomDao.findByUser1AndUser2(user1, user2);
        if (existing.isPresent()) return existing.get().getUniqueId();

        // 채널 식별자용 UUID 생성
        String uniqueId = UUID.randomUUID().toString();

        // SQL 저장 (Ncloud 채널 생성은 프론트에서 처리)
        ChatRoom room = ChatRoom.builder()
                .user1(user1)
                .user2(user2)
                .uniqueId(uniqueId)
                .build();

        chatRoomDao.save(room);

        return room.getUniqueId();
    }


    @Transactional
    public List<String> findAllMyChatRoomIds(Integer currentUserId) {
        User me = userDao.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음: " + currentUserId));

        return chatRoomDao.findAllByUser1OrUser2(me, me).stream()
                .map(ChatRoom::getUniqueId)
                .collect(Collectors.toList());
    }


}
