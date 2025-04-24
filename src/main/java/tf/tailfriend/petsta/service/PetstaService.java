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
        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

        // 2. 유저 게시글 조회
        List<PetstaPost> posts = petstaPostDao.findByUserIdOrderByCreatedAtDesc(userId);

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
    public List<PetstaFollowingUserDto> getFollowersWithFollowingStatus(Integer targetUserId, Integer currentUserId) {
        Pageable limit = PageRequest.of(0, 20);

        // 1의 팔로워(2)들
        List<UserFollow> followerRelations = userFollowDao.findTop20ByFollowedId(targetUserId, limit);
        List<User> followerUsers = followerRelations.stream()
                .map(UserFollow::getFollower)
                .collect(Collectors.toList());

        // 3이 그들을 팔로우 중인지 (맞팔 여부)
        List<UserFollow> myFollowings = userFollowDao.findByFollowerIdAndFollowedIdIn(
                currentUserId,
                followerUsers.stream().map(User::getId).collect(Collectors.toList())
        );
        Set<Integer> myFollowingIds = myFollowings.stream()
                .map(f -> f.getFollowed().getId())
                .collect(Collectors.toSet());

        return followerUsers.stream()
                .map(user -> {
                    Integer followerId = user.getId();
                    boolean isFollowed = myFollowingIds.contains(followerId);
                    boolean isVisited = redisService.hasVisitedStory(followerId, currentUserId);
                    return new PetstaFollowingUserDto(
                            followerId,
                            user.getNickname(),
                            storageService.generatePresignedUrl(user.getFile().getPath()),
                            isFollowed,
                            isVisited
                    );
                })
                .collect(Collectors.toList());
    }


    @Transactional
    public List<PetstaFollowingUserDto> getFollowingsWithFollowingStatus(Integer targetUserId, Integer currentUserId) {
        Pageable limit = PageRequest.of(0, 20);

        // 1이 팔로우 중인 유저들(4)
        List<UserFollow> followingRelations = userFollowDao.findTop20ByFollowerId(targetUserId, limit);
        List<User> followingUsers = followingRelations.stream()
                .map(UserFollow::getFollowed)
                .collect(Collectors.toList());

        // 3이 그들을 팔로우 중인지
        List<UserFollow> myFollowings = userFollowDao.findByFollowerIdAndFollowedIdIn(
                currentUserId,
                followingUsers.stream().map(User::getId).collect(Collectors.toList())
        );
        Set<Integer> myFollowingIds = myFollowings.stream()
                .map(f -> f.getFollowed().getId())
                .collect(Collectors.toSet());

        return followingUsers.stream()
                .map(user -> {
                    Integer followedId = user.getId();
                    boolean isFollowed = myFollowingIds.contains(followedId);
                    boolean isVisited = redisService.hasVisitedStory(followedId, currentUserId);

                    return new PetstaFollowingUserDto(
                            followedId,
                            user.getNickname(),
                            storageService.generatePresignedUrl(user.getFile().getPath()),
                            isFollowed,
                            isVisited
                    );
                })
                .collect(Collectors.toList());

    }


}
