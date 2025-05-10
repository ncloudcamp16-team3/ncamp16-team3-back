package tf.tailfriend.user.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tf.tailfriend.board.entity.*;
import tf.tailfriend.board.repository.*;
import tf.tailfriend.chat.entity.ChatRoom;
import tf.tailfriend.chat.repository.ChatRoomDao;
import tf.tailfriend.chat.repository.TradeMatchDao;
import tf.tailfriend.file.entity.File;
import tf.tailfriend.file.repository.FileDao;
import tf.tailfriend.global.service.StorageService;
import tf.tailfriend.global.service.StorageServiceException;
import tf.tailfriend.notification.repository.NotificationDao;
import tf.tailfriend.pet.entity.Pet;
import tf.tailfriend.pet.entity.PetPhoto;

import tf.tailfriend.pet.repository.PetDao;
import tf.tailfriend.pet.repository.PetMatchDao;
import tf.tailfriend.petsitter.repository.PetSitterDao;
import tf.tailfriend.petsta.entity.*;
import tf.tailfriend.petsta.repository.PetstaBookmarkDao;
import tf.tailfriend.petsta.repository.PetstaCommentDao;
import tf.tailfriend.petsta.repository.PetstaLikeDao;
import tf.tailfriend.petsta.repository.PetstaPostDao;
import tf.tailfriend.reserve.entity.Reserve;
import tf.tailfriend.reserve.repository.PaymentDao;
import tf.tailfriend.reserve.repository.ReserveDao;
import tf.tailfriend.schedule.entity.Schedule;
import tf.tailfriend.schedule.repository.ScheduleDao;
import tf.tailfriend.user.entity.User;
import tf.tailfriend.user.entity.UserFollow;
import tf.tailfriend.user.entity.dto.*;
import tf.tailfriend.user.exception.UserException;
import tf.tailfriend.user.exception.UserSaveException;
import tf.tailfriend.user.repository.UserDao;
import tf.tailfriend.user.repository.UserFollowDao;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserDao userDao;
    private final PetSitterDao petSitterDao;
    private final FileDao fileDao;
    private final UserFollowDao userFollowDao;
    private final StorageService storageService;
    private final PetstaLikeDao petstaLikeDao;
    private final BoardLikeDao boardLikeDao;
    private final ChatRoomDao chatRoomDao;
    private final TradeMatchDao tradeMatchDao;
    private final PetMatchDao petMatchDao;
    private final CommentDao commentDao;
    private final PetstaCommentDao petstaCommentDao;
    private final PetstaPostDao petstaPostDao;
    private final BoardBookmarkDao boardBookmarkDao;
    private final PetstaBookmarkDao petstaBookmarkDao;
    private final BoardDao boardDao;
    private final ProductDao productDao;
    private final PetDao petDao;
    private final NotificationDao notificationDao;
    private final ReserveDao reserveDao;
    private final ScheduleDao scheduleDao;


    @PersistenceContext
    private EntityManager entityManager;

    //회원의 마이페이지 정보 조회
    public MypageResponseDto getMemberInfo(Integer userId) {
        // 1. 회원 정보 조회 (탈퇴하지 않은 회원만)
        User user = userDao.findByIdAndDeletedFalse(userId)
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
        // 탈퇴하지 않은 회원만 조회
        User user = userDao.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다: " + userId));

        // 1. 닉네임 유효성 검사
        if (newNickname == null || newNickname.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임은 비어있을 수 없습니다.");
        }

        // 2. 닉네임 길이 제한
        if (newNickname.length() < 2 || newNickname.length() > 8) {
            throw new IllegalArgumentException("닉네임은 2-8자 사이여야 합니다.");
        }

        // 3. 닉네임 중복 검사
        userDao.findByNicknameAndDeletedFalse(newNickname)
                .filter(u -> !u.getId().equals(userId))
                .ifPresent(u -> {
                    throw new IllegalArgumentException("이미 사용 중인 닉네임입니다: " + newNickname);
                });

        // 4. 닉네임 업데이트 및 저장
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
    public List<String> withdrawMember(Integer userId) {
        List<String> channelNames;
        try {
            User user = userDao.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

            List<PetstaCommentMention> mentions = petstaCommentDao.findMentionsByUserId(userId);

            for (PetstaCommentMention mention : mentions) {
                PetstaComment comment = mention.getComment();
                String originalContent = comment.getContent();

                // @닉네임 ➝ @삭제된유저 로 바꾸기
                String replacedContent = originalContent.replaceFirst("@" + mention.getMentionedNickname(), "@삭제된유저");

                comment.setContent(replacedContent);
                petstaCommentDao.save(comment);
            }

            // 펫스타 좋아요 감소
            List<PetstaLike> likes = petstaLikeDao.findAllByUserIdWithPost(userId);

            for (PetstaLike like : likes) {
                PetstaPost post = like.getPetstaPost();
                post.decreaseLikeCount(); // 내부적으로 0 미만으로 내려가지 않음
            }

             petstaLikeDao.deleteAll(likes);

            // 펫스타 북마크 제거
            List<PetstaBookmark> petstaBookmarks = petstaBookmarkDao.findAllByUserIdWithPost(userId);

            for (PetstaBookmark bookmark : petstaBookmarks) {
                PetstaPost post = bookmark.getPetstaPost();
            }

            petstaBookmarkDao.deleteAll(petstaBookmarks);

            // 게시판 좋아요 감소
            List<BoardLike> boardLikes = boardLikeDao.findAllByUserIdWithBoard(userId);

            for (BoardLike like : boardLikes) {
                Board board = like.getBoard();
                board.decreaseLikeCount(); // 최소 0 미만 방지용 조건 있으면 추가
            }

            boardLikeDao.deleteAll(boardLikes); // 좋아요 기록 삭제

            List<BoardBookmark> boardBookmarks = boardBookmarkDao.findAllByUserIdWithBoard(userId);

            for (BoardBookmark bookmark : boardBookmarks) {
                Board board = bookmark.getBoard();
            }

            boardBookmarkDao.deleteAll(boardBookmarks); // 북마크 엔티티 삭제


            // 1. 내가 팔로우한 유저들의 followerCount 감소
            List<UserFollow> followings = userFollowDao.findAllByFollowerId(userId);
            for (UserFollow follow : followings) {
                User followedUser = follow.getFollowed();
                if (followedUser.getFollowerCount() > 0) {
                    followedUser.setFollowerCount(followedUser.getFollowerCount() - 1);
                }
            }

            // 2. 나를 팔로우한 유저들의 followCount 감소
            List<UserFollow> followers = userFollowDao.findAllByFollowedId(userId);
            for (UserFollow follow : followers) {
                User followerUser = follow.getFollower();
                if (followerUser.getFollowCount() > 0) {
                    followerUser.setFollowCount(followerUser.getFollowCount() - 1);
                }
            }

            // 3. 팔로우 정보 삭제
            userFollowDao.deleteAll(followings);
            userFollowDao.deleteAll(followers);
            user.setFollowCount(0);
            user.setFollowerCount(0);
            // 1. ChatRoom 삭제 + 채널 ID 수집
            List<ChatRoom> rooms = chatRoomDao.findAllByUserId(userId);
            channelNames = rooms.stream()
                    .map(room -> "room-" + room.getUniqueId())
                    .collect(Collectors.toList());
            chatRoomDao.deleteAll(rooms);

            // 2. TradeMatch 삭제
            tradeMatchDao.deleteAll(tradeMatchDao.findAllByUserId(userId));

            // 3. PetMatch 삭제
            petMatchDao.deleteAll(petMatchDao.findAllByUserId(userId));

            // 게시판 댓글 soft delete + board의 댓글 수 감소
            List<Comment> comments = commentDao.findAllActiveByUserIdWithBoard(userId);
            for (Comment comment : comments) {
                Board board = comment.getBoard();
                board.decreaseCommentCount(); // 내부적으로 음수 방지 처리 권장
                comment.setDeleted();         // 내용 비우고 삭제 플래그 설정
            }

            List<PetstaComment> petstaComments = petstaCommentDao.findAllWithRepliesByUserId(userId);
            for (PetstaComment comment : petstaComments) {

                // 이미 소프트 삭제된 댓글은 건너뜀
                if (comment.isDeleted()) {
                    continue;
                }

                // 🔽 댓글 수 감소
                petstaPostDao.decrementCommentCount(comment.getPost().getId());

                // 🔽 대댓글이면 부모 댓글의 replyCount 감소
                if (comment.getParent() != null) {
                    PetstaComment parent = comment.getParent();
                    parent.setReplyCount(Math.max(0, parent.getReplyCount() - 1));
                    petstaCommentDao.save(parent);
                }

                comment.markAsDeleted();
                comment.clearMention();
                petstaCommentDao.save(comment);
            }



            List<PetstaPost> posts = petstaPostDao.findAllByUserId(userId);

            for (PetstaPost post : posts) {
                Integer postId = post.getId();

                // 좋아요, 북마크 삭제
                petstaLikeDao.deleteAllByPostId(postId);
                petstaBookmarkDao.deleteAllByPostId(postId);

                // 게시글 soft delete
                post.markAsDeleted();
                petstaPostDao.save(post);
            }

            // 게시글 수 0으로 초기화
            user.setPostCount(0);

            List<Board> boards = boardDao.findAllByUserIdWithPhotos(userId);

            for (Board board : boards) {
                // 좋아요, 북마크, 댓글 삭제
                boardLikeDao.deleteAllByBoard(board);
                boardBookmarkDao.deleteAllByBoard(board);
                commentDao.deleteAllByBoard(board);

                // boardTypeId == 2 ➝ 중고 상품이라면 product 삭제
                if (board.getBoardType().getId().equals(2)) {
                    productDao.deleteByBoard(board); // or findByBoardId → delete
                }

                // 사진 S3 삭제
                for (BoardPhoto photo : board.getPhotos()) {
                    storageService.delete(photo.getFile().getPath());
                }

                // 게시글 삭제
                tradeMatchDao.deleteByPostId(board.getId());
                boardDao.delete(board);
            }

            //내 펫 삭제
            List<Pet> pets = petDao.findAllByUserId(userId);
            petDao.deleteAll(pets);

            //내 모든 알림 삭제
            notificationDao.deleteByUserId(userId);


            List<Reserve> reserves = reserveDao.findAllByUserId(userId);
            reserveDao.deleteAll(reserves);

            petSitterDao.findById(userId).ifPresent(petSitter -> {
                petSitter.setFile(null);       // 🔥 file 연결 끊기
                petSitterDao.save(petSitter);  // 🔄 update로 null 반영
                petSitterDao.delete(petSitter); // ✅ 이제 삭제 가능
            });
            List<Schedule> schedules = scheduleDao.findByUserId(userId);
            scheduleDao.deleteAll(schedules);

            user.setNickname("deleted-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20));
            user.setSnsAccountId("deleted-" + UUID.randomUUID().toString().replace("-", ""));
            user.setFile(fileDao.getFileById(1));
            user.setDeleted(true);//

        } catch (Exception e) {
            log.error("회원 탈퇴 중 오류 발생: {}", e.getMessage(), e);
            throw new IllegalStateException("회원 탈퇴 처리 중 오류가 발생했습니다: " + e.getMessage());
        } catch (StorageServiceException e) {
            throw new RuntimeException(e);
        }
        return channelNames;
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

        } catch (Exception e) {
            throw new UserSaveException();
        }
    }
}
