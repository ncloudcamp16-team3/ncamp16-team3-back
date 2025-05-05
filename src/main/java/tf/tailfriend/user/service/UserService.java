package tf.tailfriend.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tf.tailfriend.board.entity.Board;
import tf.tailfriend.board.repository.BoardBookmarkDao;
import tf.tailfriend.board.repository.BoardDao;
import tf.tailfriend.board.repository.BoardLikeDao;
import tf.tailfriend.board.repository.CommentDao;
import tf.tailfriend.chat.entity.ChatRoom;
import tf.tailfriend.chat.repository.ChatRoomDao;
import tf.tailfriend.chat.repository.TradeMatchDao;
import tf.tailfriend.file.entity.File;
import tf.tailfriend.file.repository.FileDao;
import tf.tailfriend.global.service.StorageService;
import tf.tailfriend.notification.repository.NotificationDao;
import tf.tailfriend.pet.entity.Pet;
import tf.tailfriend.pet.entity.PetPhoto;
import tf.tailfriend.pet.repository.PetPhotoDao;
import tf.tailfriend.pet.repository.PetDao;
import tf.tailfriend.petsitter.repository.PetSitterDao;
import tf.tailfriend.petsta.repository.PetstaBookmarkDao;
import tf.tailfriend.petsta.repository.PetstaCommentDao;
import tf.tailfriend.petsta.repository.PetstaLikeDao;
import tf.tailfriend.petsta.repository.PetstaPostDao;
import tf.tailfriend.reserve.entity.Payment;
import tf.tailfriend.reserve.entity.Reserve;
import tf.tailfriend.reserve.repository.PaymentDao;
import tf.tailfriend.reserve.repository.ReserveDao;
import tf.tailfriend.schedule.repository.ScheduleDao;
import tf.tailfriend.user.entity.User;
import tf.tailfriend.user.entity.UserFollow;
import tf.tailfriend.user.entity.dto.*;
import tf.tailfriend.user.exception.UserException;
import tf.tailfriend.user.exception.UserSaveException;
import tf.tailfriend.user.repository.UserDao;
import tf.tailfriend.user.repository.UserFollowDao;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserDao userDao;
    private final PetSitterDao petSitterDao;
    private final FileDao fileDao;
    private final PetDao petDao;
    private final PetPhotoDao petPhotoDao;
    private final UserFollowDao userFollowDao;
    private final StorageService storageService;
    private final BoardBookmarkDao boardBookmarkDao;
    private final BoardDao boardDao;
    private final BoardLikeDao boardLikeDao;
    private final CommentDao commentDao;
    private final ChatRoomDao chatRoomDao;
    private final PetstaBookmarkDao petstaBookmarkDao;
    private final PetstaLikeDao petstaLikeDao;
    private final PetstaCommentDao petstaCommentDao;
    private final PetstaPostDao petstaPostDao;
    private final TradeMatchDao tradeMatchDao;
    private final ScheduleDao scheduleDao;
    private final ReserveDao reserveDao; // 추가
    private final PaymentDao paymentDao;
    private final NotificationDao notificationDao;



    //회원의 마이페이지 정보 조회
    public MypageResponseDto getMemberInfo(Integer userId) {
        // 1. 회원 정보 조회
        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다: " + userId));

        // 2. 반려동물 정보 변환
        List<PetResponseDto> petDtos = user.getPet().stream()
                .map(this::convertToPetDto)
                .collect(Collectors.toList());

        // 3. 펫시터 여부 확인
        boolean isSitter = petSitterDao.existsById(userId);

        // 4. 응답 DTO 생성 및 반환
        return MypageResponseDto.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(storageService.generatePresignedUrl(user.getFile().getPath()))
                .pets(petDtos)
                .isSitter(isSitter)
                .build();
    }

    // 회원의 닉네임 업데이트
    @Transactional
    public String updateNickname(Integer userId, String newNickname) {
        // 1. 닉네임 유효성 검사
        if (newNickname == null || newNickname.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임은 비어있을 수 없습니다.");
        }

        // 2. 닉네임 길이 제한
        if (newNickname.length() < 2 || newNickname.length() > 20) {
            throw new IllegalArgumentException("닉네임은 2-20자 사이여야 합니다.");
        }

        // 3. 회원 조회
        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다: " + userId));

        // 4. 닉네임 중복 검사
        userDao.findByNickname(newNickname)
                .filter(u -> !u.getId().equals(userId))
                .ifPresent(u -> {
                    throw new IllegalArgumentException("이미 사용 중인 닉네임입니다: " + newNickname);
                });

        // 5. 닉네임 업데이트
        if (user != null) {
            user.updateNickname(newNickname);
        } else {
            throw new UnsupportedOperationException("닉네임 업데이트를 할 수 없습니다.");
        }

        // 6. 저장 및 반환
        user.updateNickname(newNickname);
        userDao.save(user);
        return newNickname;
    }

    // 회원 프로필 이미지 업데이트
    @Transactional
    public String updateProfileImage(Integer userId, Integer fileId) {
        // 1. 회원 조회
        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다: " + userId));

        // 2. 파일 조회
        File file = fileDao.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 파일입니다: " + fileId));

        // 3. 프로필 이미지 업데이트
        user.updateProfileImage(file);

        // 4. 저장 및 URL 반환
        userDao.save(user);
        return file.getPath();
    }

    //회원을 탈퇴
    @Transactional
    public void withdrawMember(Integer userId) {
        // 1. 회원 조회
        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다: " + userId));

        // 2. 예약과 결제 데이터 삭제 (추가)
        List<Reserve> userReserves = reserveDao.findByUserId(userId);
        for (Reserve reserve : userReserves) {
            // 결제 내역 삭제
            Payment payment = paymentDao.findByReserveId(reserve.getId()).orElse(null);
            if (payment != null) {
                paymentDao.delete(payment);
            }
            // 예약 삭제
            reserveDao.delete(reserve);
        }

        // 3. 일정(스케줄) 데이터 삭제
        scheduleDao.deleteByUserId(userId);

        // 4. 거래 매칭 데이터 삭제
        tradeMatchDao.deleteByUserId(userId);

        // 5. PetSta 북마크 삭제
        petstaBookmarkDao.deleteByUserId(userId);

        // 6. PetSta 좋아요 삭제
        petstaLikeDao.deleteByUserId(userId);


        // 1. 내가 작성한 게시글 ID들 조회
        List<Integer> postIds = petstaPostDao.findIdsByUserId(userId);

        // 2. 각 게시글에 달린 댓글 먼저 삭제
        for (Integer postId : postIds) {
            petstaCommentDao.deleteRepliesByPostId(postId);

            // 2. 그다음 부모 댓글들(= parent == null) 삭제
            petstaCommentDao.deleteParentsByPostId(postId);
            petstaCommentDao.deleteByPostId(postId);
        }

        // 8. PetSta 포스트 삭제
        petstaPostDao.deleteByUserId(userId);

        // 9. 일정 삭제
        notificationDao.deleteByUserId(userId);

        // 9. 사용자의 채팅방 및 메시지 처리
        List<ChatRoom> userChatRooms = chatRoomDao.findAllByUser1OrUser2(user, user);
        for (ChatRoom chatRoom : userChatRooms) {
            // 채팅방 삭제
            chatRoomDao.delete(chatRoom);
        }

        // 10. 사용자가 작성한 게시글 처리
        List<Board> userBoards = boardDao.findByUserIdOrderByCreatedAtDesc(userId);
        for (Board board : userBoards) {
            // 게시글과 관련된 모든 데이터 삭제
            boardBookmarkDao.deleteAllByBoard(board);
            boardLikeDao.deleteAllByBoard(board);
            commentDao.deleteAllByBoard(board);
            // 게시글 삭제
            boardDao.delete(board);
        }

        notificationDao.deleteByUserId(userId);
        // 11. 게시판 북마크 삭제
        boardBookmarkDao.deleteByUserId(userId);

        // 12. 팔로우 관계 삭제 (팔로워 및 팔로잉)
        userFollowDao.deleteByFollowerId(userId);
        userFollowDao.deleteByFollowedId(userId);

        // 13. 펫시터 정보가 있다면 함께 삭제
        petSitterDao.findById(userId).ifPresent(petSitterDao::delete);

        // 14. 반려동물 관련 데이터 삭제
        user.getPet().forEach(pet -> {
            // 반려동물 사진 삭제
            petPhotoDao.deleteByPetId(pet.getId());

            // 반려동물 삭제
            petDao.delete(pet);
        });

        // 일정 삭제
        notificationDao.deleteByUserId(userId);

        // 15. 회원 삭제
        userDao.delete(user);
    }

    private PetResponseDto convertToPetDto(Pet pet) {
        // 1. 반려동물 썸네일 이미지 URL 찾기
        String petProfileImageUrl = pet.getPhotos().stream()
                .filter(PetPhoto::isThumbnail)
                .findFirst()
                .or(() -> pet.getPhotos().stream().findFirst())
                .map(photo -> storageService.generatePresignedUrl(photo.getFile().getPath()))
                .orElse(null);

        // 2. PetResponseDto 생성 및 반환
        return PetResponseDto.builder()
                .id(pet.getId())
                .name(pet.getName())
                .type(pet.getPetType().getName())
                .gender(pet.getGender())
                .birth(pet.getBirth())
                .weight(pet.getWeight())
                .info(pet.getInfo())
                .neutered(pet.getNeutered())
                .profileImageUrl(petProfileImageUrl)
                .build();
    }

    @Transactional
    public void toggleFollow(Integer followerId, Integer followedId) {
        User followerUser = userDao.findById(followerId)
                .orElseThrow(() -> new IllegalArgumentException("팔로우하는 유저를 찾을 수 없습니다."));

        User followedUser = userDao.findById(followedId)
                .orElseThrow(() -> new IllegalArgumentException("팔로우받는 유저를 찾을 수 없습니다."));

        Optional<UserFollow> existingFollow = userFollowDao.findByFollowerIdAndFollowedId(followerId, followedId);

        if (existingFollow.isPresent()) {
            userFollowDao.delete(existingFollow.get());

            userDao.decrementFollowCount(followerId);   // 내가 언팔 → 팔로우 수 감소
            userDao.decrementFollowerCount(followedId); // 상대방 → 팔로워 수 감소

        } else {
            UserFollow newFollow = UserFollow.of(followerUser, followedUser);
            userFollowDao.save(newFollow);

            userDao.incrementFollowCount(followerId);   // 내가 팔로우 → 팔로우 수 증가
            userDao.incrementFollowerCount(followedId); // 상대방 → 팔로워 수 증가
        }
    }

    @Transactional
    public String getUsername(Integer userId) {
        return userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."))
                .getNickname(); // ← 여기서 닉네임만 추출
    }
  
    public void userInfoSave(UserInfoDto userInfoDto) {

        User userEntity = userDao.findById(userInfoDto.getId())
                .orElseThrow(() -> new UserException());

        try {
            User updatedUser = userEntity.toBuilder()
                    .nickname(userInfoDto.getNickname())
                    .distance(userInfoDto.getDistance())
                    .latitude(userInfoDto.getLatitude())
                    .longitude(userInfoDto.getLongitude())
                    .address(userInfoDto.getAddress())
                    .dongName(userInfoDto.getDongName())
                    .build();

            userDao.save(updatedUser);

        }  catch (Exception e) {
            throw new UserSaveException();
        }
    }
}
