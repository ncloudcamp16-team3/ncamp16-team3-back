package tf.tailfriend.petsta.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tf.tailfriend.global.service.RedisService;
import tf.tailfriend.global.service.StorageService;
import tf.tailfriend.petsta.entity.PetstaPost;
import tf.tailfriend.petsta.entity.dto.PetstaFollowingUserDto;
import tf.tailfriend.petsta.entity.dto.PetstaSimplePostDto;
import tf.tailfriend.petsta.entity.dto.PetstaUpdatedUserDto;
import tf.tailfriend.petsta.entity.dto.PetstaUserpageResponseDto;
import tf.tailfriend.petsta.repository.PetstaPostDao;
import tf.tailfriend.user.entity.User;
import tf.tailfriend.user.entity.UserFollow;
import tf.tailfriend.user.repository.UserDao;
import tf.tailfriend.user.repository.UserFollowDao;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PetstaService {

    private final UserDao userDao;
    private final PetstaPostDao petstaPostDao;
    private final StorageService storageService;
    private final UserFollowDao userFollowDao;
    private final RedisService redisService;


    @Transactional
    public PetstaUserpageResponseDto getUserPage(Integer currentId, Integer userId) {
        // 1. 유저 조회
        User user = userDao.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new IllegalArgumentException("탈퇴한 유저이거나 존재하지 않는 유저입니다."));


        // 2. 유저 게시글 조회
        List<PetstaPost> posts = petstaPostDao.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(userId);

        // 3. 요약 DTO로 변환
        List<PetstaSimplePostDto> postDtos = posts.stream()
                .map(post -> {
                    String filePath = post.getThumbnailFile() != null
                            ? post.getThumbnailFile().getPath()
                            : post.getFile().getPath();

                    String fileUrl = storageService.generatePresignedUrl(filePath);
                    return new PetstaSimplePostDto(post.getId(), fileUrl);
                })
                .toList();

        // 4. currentId가 userId를 팔로우하는지 여부 확인
        boolean isFollow = userFollowDao.existsByFollowerIdAndFollowedId(currentId, userId);

        //스토리 방문 처리
        if (!currentId.equals(userId)) {
            redisService.markStoryVisited(userId, currentId);
        }

        // 5. 최종 마이페이지 DTO 생성
        return new PetstaUserpageResponseDto(
                user.getId(),
                user.getNickname(),
                storageService.generatePresignedUrl(user.getFile().getPath()),
                user.getPostCount(),
                user.getFollowerCount(),
                user.getFollowCount(),
                postDtos,
                isFollow
        );
    }



    @Transactional
    public List<PetstaUpdatedUserDto> getTopFollowedUsers(Integer currentUserId) {
        Pageable limitTen = PageRequest.of(0, 10);
        List<User> followedUsers = userFollowDao.findTop10ByFollowerId(currentUserId, limitTen);

        return followedUsers.stream()
                .map(user -> {
                    boolean isVisited = redisService.hasVisitedStory(user.getId(), currentUserId);

                    return new PetstaUpdatedUserDto(
                            user.getId(),
                            user.getNickname(),
                            storageService.generatePresignedUrl(user.getFile().getPath()),
                            isVisited
                    );
                })
                .toList();
    }

    @Transactional
    public List<PetstaFollowingUserDto> getFollowersWithFollowingStatus(
            Integer targetUserId, Integer currentUserId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        List<UserFollow> followerRelations = userFollowDao.findByFollowedId(targetUserId, pageable);
        List<User> followerUsers = followerRelations.stream()
                .map(UserFollow::getFollower)
                .collect(Collectors.toList());

        List<UserFollow> myFollowings = userFollowDao.findByFollowerIdAndFollowedIdIn(
                currentUserId,
                followerUsers.stream().map(User::getId).collect(Collectors.toList())
        );
        Set<Integer> myFollowingIds = myFollowings.stream()
                .map(f -> f.getFollowed().getId())
                .collect(Collectors.toSet());

        return followerUsers.stream()
                .map(user -> new PetstaFollowingUserDto(
                        user.getId(),
                        user.getNickname(),
                        storageService.generatePresignedUrl(user.getFile().getPath()),
                        myFollowingIds.contains(user.getId()),
                        redisService.hasVisitedStory(user.getId(), currentUserId)
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public List<PetstaFollowingUserDto> getFollowingsWithFollowingStatus(
            Integer targetUserId, Integer currentUserId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        List<UserFollow> followingRelations = userFollowDao.findByFollowerId(targetUserId, pageable);
        List<User> followingUsers = followingRelations.stream()
                .map(UserFollow::getFollowed)
                .collect(Collectors.toList());

        List<UserFollow> myFollowings = userFollowDao.findByFollowerIdAndFollowedIdIn(
                currentUserId,
                followingUsers.stream().map(User::getId).collect(Collectors.toList())
        );
        Set<Integer> myFollowingIds = myFollowings.stream()
                .map(f -> f.getFollowed().getId())
                .collect(Collectors.toSet());

        return followingUsers.stream()
                .map(user -> new PetstaFollowingUserDto(
                        user.getId(),
                        user.getNickname(),
                        storageService.generatePresignedUrl(user.getFile().getPath()),
                        myFollowingIds.contains(user.getId()),
                        redisService.hasVisitedStory(user.getId(), currentUserId)
                ))
                .collect(Collectors.toList());
    }



}
