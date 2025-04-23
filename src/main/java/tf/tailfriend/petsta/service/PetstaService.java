package tf.tailfriend.petsta.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tf.tailfriend.global.service.StorageService;
import tf.tailfriend.petsta.entity.PetstaBookmark;
import tf.tailfriend.petsta.entity.PetstaPost;
import tf.tailfriend.petsta.entity.dto.PetstaSimplePostDto;
import tf.tailfriend.petsta.entity.dto.PetstaUpdatedUserDto;
import tf.tailfriend.petsta.entity.dto.PetstaUserpageResponseDto;
import tf.tailfriend.petsta.repository.PetstaDao;
import tf.tailfriend.petsta.repository.PetstaPostDao;
import tf.tailfriend.user.entity.User;
import tf.tailfriend.user.repository.UserDao;
import tf.tailfriend.user.repository.UserFollowDao;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PetstaService {

    private final UserDao userDao;
    private final PetstaPostDao petstaPostDao;
    private final StorageService storageService;
    private final UserFollowDao userFollowDao;


    @Transactional
    public PetstaUserpageResponseDto getUserPage(Integer currentId, Integer userId) {
        // 1. 유저 조회
        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

        // 2. 유저 게시글 조회
        List<PetstaPost> posts = petstaPostDao.findByUserIdOrderByCreatedAtDesc(userId); // ← 커스텀 쿼리 필요

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

        // 4. 최종 마이페이지 DTO 생성
        return new PetstaUserpageResponseDto(
                user.getId(),
                user.getNickname(),
                storageService.generatePresignedUrl(user.getFile().getPath()),
                user.getPostCount(),
                user.getFollowerCount(),
                user.getFollowCount(),
                postDtos
        );
    }


    @Transactional
    public List<PetstaUpdatedUserDto> getFollowedUsers(Integer currentUserId) {
        Pageable limitTen = PageRequest.of(0, 10);
        List<User> followedUsers = userFollowDao.findTop10ByFollowerId(currentUserId, limitTen);

        return followedUsers.stream()
                .map(user -> new PetstaUpdatedUserDto(
                        user.getId(),
                        user.getNickname(),
                        storageService.generatePresignedUrl(user.getFile().getPath())
                ))
                .toList();
    }



}
