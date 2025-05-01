package tf.tailfriend.chat.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tf.tailfriend.board.entity.Board;
import tf.tailfriend.board.repository.BoardDao;
import tf.tailfriend.chat.entity.ChatRoom;
import tf.tailfriend.chat.entity.TradeMatch;
import tf.tailfriend.chat.entity.dto.ChatRoomListResponseDto;
import tf.tailfriend.chat.repository.ChatRoomDao;
import tf.tailfriend.chat.repository.TradeMatchDao;
import tf.tailfriend.global.service.StorageService;
import tf.tailfriend.pet.entity.Pet;
import tf.tailfriend.pet.entity.PetMatch;
import tf.tailfriend.pet.repository.PetDao;
import tf.tailfriend.pet.repository.PetMatchDao;
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
    private final PetDao petDao;
    private final BoardDao boardDao;
    private final PetMatchDao petMatchDao;
    private final StorageService storageService;
    private final TradeMatchDao tradeMatchDao;

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
    public void checkOrCreateMatch(Integer petId1, Integer petId2) {
        Integer minId = Math.min(petId1, petId2);
        Integer maxId = Math.max(petId1, petId2);

        boolean exists = petMatchDao.existsByPet1IdAndPet2Id(minId, maxId);

        if (!exists) {
            Pet pet1 = petDao.findById(minId)
                    .orElseThrow(() -> new IllegalArgumentException("Pet not found: " + minId));
            Pet pet2 = petDao.findById(maxId)
                    .orElseThrow(() -> new IllegalArgumentException("Pet not found: " + maxId));

            PetMatch match = PetMatch.of(pet1, pet2);
            petMatchDao.save(match);
        }
    }


    @Transactional
    public boolean isMatched(Integer petId1, Integer petId2) {
        Integer minId = Math.min(petId1, petId2);
        Integer maxId = Math.max(petId1, petId2);
        return petMatchDao.existsByPet1IdAndPet2Id(minId, maxId);
    }


    @Transactional
    public List<ChatRoomListResponseDto> findAllMyChatRooms(Integer currentUserId) {
        User me = userDao.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음: " + currentUserId));

        return chatRoomDao.findAllByUser1OrUser2(me, me).stream()
                .map(room -> {
                    User partner;
                    if (room.getUser1().getId().equals(currentUserId)) {
                        partner = room.getUser2();
                    } else {
                        partner = room.getUser1();
                    }
                    return new ChatRoomListResponseDto(
                            room.getUniqueId(),
                            partner.getNickname(),
                            storageService.generatePresignedUrl(partner.getFile().getPath())
                    );
                })
                .toList();
    }

    @Transactional
    public void checkOrCreateTradeMatch(Integer userId, Integer postId) {
        if (tradeMatchDao.existsByUserIdAndPostId(userId, postId)) {
            return;
        }

        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        Board board = boardDao.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        TradeMatch match = TradeMatch.of(user, board.getId());
        tradeMatchDao.save(match);
    }

    /**
     * 거래 매칭 여부 확인
     */
    @Transactional
    public boolean isTradeMatched(Integer userId, Integer postId) {
        return tradeMatchDao.existsByUserIdAndPostId(userId, postId);
    }

}
