package tf.tailfriend.user.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tf.tailfriend.file.entity.File;
import tf.tailfriend.file.repository.FileDao;
import tf.tailfriend.global.service.StorageService;
import tf.tailfriend.pet.entity.Pet;
import tf.tailfriend.pet.entity.PetPhoto;

import tf.tailfriend.petsitter.repository.PetSitterDao;
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
    private final UserFollowDao userFollowDao;
    private final StorageService storageService;

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
        if (newNickname.length() < 2 || newNickname.length() > 20) {
            throw new IllegalArgumentException("닉네임은 2-20자 사이여야 합니다.");
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
    public void withdrawMember(Integer userId) {
        try {
            // 시스템 사용자 ID 설정 (탈퇴한 회원 데이터 소유권 이전용)
            Integer systemUserId = 9999999;

            // 펫스타 탈퇴 회원이 좋아요 한 게시글의 카운트 감소
            try {
                entityManager.createNativeQuery(
                                "UPDATE petsta_posts pp " +
                                        "JOIN petsta_likes pl ON pp.id = pl.petsta_post_id " +
                                        "SET pp.like_count = GREATEST(pp.like_count - 1, 0) " +
                                        "WHERE pl.user_id = ?")
                        .setParameter(1, userId)
                        .executeUpdate();
            } catch (Exception e) {
                log.error("펫스타 좋아요 카운트 조정 중 오류: {}", e.getMessage());
            }

            //게시판 탈퇴 회원이 좋아요 한 게시글의 카운트 감소
            try {
                entityManager.createNativeQuery(
                                "UPDATE boards b " +
                                        "JOIN board_likes bl ON b.id = bl.board_post_id " +
                                        "SET b.like_count = GREATEST(b.like_count - 1, 0) " +
                                        "WHERE bl.user_id = ?")
                        .setParameter(1, userId)
                        .executeUpdate();
            } catch (Exception e) {
                log.error("게시판 좋아요 카운트 조정 중 오류: {}", e.getMessage());
            }

            try {
                // 내가 팔로우하는 사람들의 팔로워 카운트 감소
                entityManager.createNativeQuery(
                                "UPDATE users u " +
                                        "JOIN user_follows uf ON u.id = uf.followed_id " +
                                        "SET u.follower_count = GREATEST(u.follower_count - 1, 0) " +
                                        "WHERE uf.follower_id = ?")
                        .setParameter(1, userId)
                        .executeUpdate();

                // 나를 팔로우하는 사람들의 팔로우 카운트 감소
                entityManager.createNativeQuery(
                                "UPDATE users u " +
                                        "JOIN user_follows uf ON u.id = uf.follower_id " +
                                        "SET u.follow_count = GREATEST(u.follow_count - 1, 0) " +
                                        "WHERE uf.followed_id = ?")
                        .setParameter(1, userId)
                        .executeUpdate();
            } catch (Exception e) {
                log.error("팔로우 카운트 조정 중 오류: {}", e.getMessage());
            }

            // 채팅방 및 메시지 처리
            try {
                // 채팅 메시지 내용 수정
                entityManager.createNativeQuery(
                                "UPDATE message SET content = '삭제된 메시지입니다', user_id = ? WHERE user_id = ?")
                        .setParameter(1, systemUserId)
                        .setParameter(2, userId)
                        .executeUpdate();

                // 채팅방은 남기고 사용자만 시스템 계정으로 변경 (user_id1)
                entityManager.createNativeQuery(
                                "UPDATE chat_rooms SET user_id1 = ? WHERE user_id1 = ?")
                        .setParameter(1, systemUserId)
                        .setParameter(2, userId)
                        .executeUpdate();

                // 채팅방은 남기고 사용자만 시스템 계정으로 변경 (user_id2)
                entityManager.createNativeQuery(
                                "UPDATE chat_rooms SET user_id2 = ? WHERE user_id2 = ?")
                        .setParameter(1, systemUserId)
                        .setParameter(2, userId)
                        .executeUpdate();

            } catch (Exception e) {
                log.error("채팅 관련 데이터 처리 중 오류: {}", e.getMessage());
            }

            try {
                // 내 게시글에 달린 댓글 삭제
                entityManager.createNativeQuery(
                                "DELETE pc FROM petsta_comments pc " +
                                        "JOIN petsta_posts pp ON pc.post_id = pp.id " +
                                        "WHERE pp.user_id = ?")
                        .setParameter(1, userId)
                        .executeUpdate();

                // 내가 작성한 댓글 처리
                entityManager.createNativeQuery(
                                "UPDATE petsta_comments SET content = '삭제된 댓글입니다', deleted = true, user_id = ? WHERE user_id = ?")
                        .setParameter(1, systemUserId)
                        .setParameter(2, userId)
                        .executeUpdate();
            } catch (Exception e) {
                log.error("펫스타 댓글 처리 중 오류: {}", e.getMessage());
            }

            // 내 펫스타 게시글에 달린 좋아요 삭제
            try {
                entityManager.createNativeQuery(
                                "DELETE pl FROM petsta_likes pl " +
                                        "JOIN petsta_posts pp ON pl.petsta_post_id = pp.id " +
                                        "WHERE pp.user_id = ?")
                        .setParameter(1, userId)
                        .executeUpdate();
            } catch (Exception e) {
                log.error("펫스타 게시글 좋아요 삭제 중 오류: {}", e.getMessage());
            }

            // 내 펫스타 게시글에 달린 북마크 삭제
            try {
                entityManager.createNativeQuery(
                                "DELETE pb FROM petsta_bookmarks pb " +
                                        "JOIN petsta_posts pp ON pb.petsta_post_id = pp.id " +
                                        "WHERE pp.user_id = ?")
                        .setParameter(1, userId)
                        .executeUpdate();
            } catch (Exception e) {
                log.error("펫스타 게시글 북마크 삭제 중 오류: {}", e.getMessage());
            }

            // 펫스타 게시글 완전 삭제
            try {
                entityManager.createNativeQuery("DELETE FROM petsta_posts WHERE user_id = ?")
                        .setParameter(1, userId)
                        .executeUpdate();
                log.info("펫스타 게시글 삭제 완료");
            } catch (Exception e) {
                log.error("펫스타 게시글 처리 중 오류: {}", e.getMessage());
            }

            // 내가 다른 사람 게시글에 누른 좋아요 삭제
            try {
                entityManager.createNativeQuery("DELETE FROM petsta_likes WHERE user_id = ?")
                        .setParameter(1, userId)
                        .executeUpdate();
            } catch (Exception e) {
                log.error("펫스타 좋아요 삭제 중 오류: {}", e.getMessage());
            }

            // 내가 다른 사람 게시글에 누른 북마크 삭제
            try {
                entityManager.createNativeQuery("DELETE FROM petsta_bookmarks WHERE user_id = ?")
                        .setParameter(1, userId)
                        .executeUpdate();
            } catch (Exception e) {
                log.error("펫스타 북마크 삭제 중 오류: {}", e.getMessage());
            }

            try {
                // 내 게시글에 달린 댓글 삭제
                entityManager.createNativeQuery(
                                "DELETE c FROM comments c " +
                                        "JOIN boards b ON c.board_id = b.id " +
                                        "WHERE b.user_id = ?")
                        .setParameter(1, userId)
                        .executeUpdate();

                // 내가 작성한 댓글 처리
                entityManager.createNativeQuery(
                                "UPDATE comments SET content = '삭제된 댓글입니다', deleted = true, user_id = ? WHERE user_id = ?")
                        .setParameter(1, systemUserId)
                        .setParameter(2, userId)
                        .executeUpdate();
            } catch (Exception e) {
                log.error("게시판 댓글 처리 중 오류: {}", e.getMessage());
            }

            // 내 게시판 게시글에 달린 좋아요 삭제
            try {
                entityManager.createNativeQuery(
                                "DELETE bl FROM board_likes bl " +
                                        "JOIN boards b ON bl.board_post_id = b.id " +
                                        "WHERE b.user_id = ?")
                        .setParameter(1, userId)
                        .executeUpdate();
            } catch (Exception e) {
                log.error("게시판 게시글 좋아요 삭제 중 오류: {}", e.getMessage());
            }

            // 내 게시판 게시글에 달린 북마크 삭제
            try {
                entityManager.createNativeQuery(
                                "DELETE bb FROM board_bookmarks bb " +
                                        "JOIN boards b ON bb.board_post_id = b.id " +
                                        "WHERE b.user_id = ?")
                        .setParameter(1, userId)
                        .executeUpdate();
            } catch (Exception e) {
                log.error("게시판 게시글 북마크 삭제 중 오류: {}", e.getMessage());
            }

            // 게시판 게시글 완전 삭제
            try {
                entityManager.createNativeQuery("DELETE FROM boards WHERE user_id = ?")
                        .setParameter(1, userId)
                        .executeUpdate();
                log.info("게시판 게시글 삭제 완료");
            } catch (Exception e) {
                log.error("게시판 게시글 처리 중 오류: {}", e.getMessage());
            }

            // 내가 다른 사람 게시글에 누른 좋아요 삭제
            try {
                entityManager.createNativeQuery("DELETE FROM board_likes WHERE user_id = ?")
                        .setParameter(1, userId)
                        .executeUpdate();
            } catch (Exception e) {
                log.error("게시판 좋아요 삭제 중 오류: {}", e.getMessage());
            }

            // 내가 다른 사람 게시글에 누른 북마크 삭제
            try {
                entityManager.createNativeQuery("DELETE FROM board_bookmarks WHERE user_id = ?")
                        .setParameter(1, userId)
                        .executeUpdate();
            } catch (Exception e) {
                log.error("게시판 북마크 삭제 중 오류: {}", e.getMessage());
            }

            try {
                // 내가 팔로우하는 사람들과의 관계 삭제
                entityManager.createNativeQuery("DELETE FROM user_follows WHERE follower_id = ?")
                        .setParameter(1, userId)
                        .executeUpdate();

                // 나를 팔로우하는 사람들과의 관계 삭제
                entityManager.createNativeQuery("DELETE FROM user_follows WHERE followed_id = ?")
                        .setParameter(1, userId)
                        .executeUpdate();
            } catch (Exception e) {
                log.error("팔로우 관계 삭제 중 오류: {}", e.getMessage());
            }

            try {
                // 결제 정보 삭제
                entityManager.createNativeQuery(
                                "DELETE p FROM payments p " +
                                        "JOIN reserves r ON p.reserve_id = r.id " +
                                        "WHERE r.user_id = ?")
                        .setParameter(1, userId)
                        .executeUpdate();

                // 예약 정보 삭제
                entityManager.createNativeQuery("DELETE FROM reserves WHERE user_id = ?")
                        .setParameter(1, userId)
                        .executeUpdate();
            } catch (Exception e) {
                log.error("예약 정보 삭제 중 오류: {}", e.getMessage());
            }

            try {
                // 반려동물 매칭 삭제
                entityManager.createNativeQuery(
                                "DELETE FROM pet_matches WHERE pet1_id IN (SELECT id FROM pets WHERE owner_id = ?) OR pet2_id IN (SELECT id FROM pets WHERE owner_id = ?)")
                        .setParameter(1, userId)
                        .setParameter(2, userId)
                        .executeUpdate();

                // 반려동물 사진 삭제
                entityManager.createNativeQuery(
                                "DELETE pp FROM pet_photos pp " +
                                        "JOIN pets p ON pp.pet_id = p.id " +
                                        "WHERE p.owner_id = ?")
                        .setParameter(1, userId)
                        .executeUpdate();

                // 반려동물
                entityManager.createNativeQuery(
                                "DELETE FROM pets WHERE owner_id = ?")
                        .setParameter(1, userId)
                        .executeUpdate();
            } catch (Exception e) {
                log.error("반려동물 처리 중 오류: {}", e.getMessage());
            }

            // 펫시터 정보 처리
            try {
                entityManager.createNativeQuery(
                                "DELETE FROM pet_sitters WHERE id = ?")
                        .setParameter(1, userId)
                        .executeUpdate();
            } catch (Exception e) {
                log.error("펫시터 처리 중 오류: {}", e.getMessage());
            }

            try {
                // 알림 데이터 삭제
                entityManager.createNativeQuery("DELETE FROM notifications WHERE user_id = ?")
                        .setParameter(1, userId)
                        .executeUpdate();

                // 거래 매칭 데이터 삭제
                entityManager.createNativeQuery("DELETE FROM trade_matches WHERE user_id = ?")
                        .setParameter(1, userId)
                        .executeUpdate();

                // 일정 데이터 삭제
                entityManager.createNativeQuery("DELETE FROM schedules WHERE user_id = ?")
                        .setParameter(1, userId)
                        .executeUpdate();
            } catch (Exception e) {
                log.error("알림, 거래 매칭, 일정 데이터 삭제 중 오류: {}", e.getMessage());
            }

            // 회원 정보 완전 삭제
            try {
                entityManager.createNativeQuery("DELETE FROM users WHERE id = ?")
                        .setParameter(1, userId)
                        .executeUpdate();

            } catch (Exception e) {
                throw e; // 회원 삭제 실패 시 예외 전파
            }
        } catch (Exception e) {
            log.error("회원 탈퇴 중 오류 발생: {}", e.getMessage(), e);
            throw new IllegalStateException("회원 탈퇴 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
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
