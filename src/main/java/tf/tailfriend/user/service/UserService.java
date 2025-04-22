package tf.tailfriend.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tf.tailfriend.file.entity.File;
import tf.tailfriend.file.repository.FileDao;
import tf.tailfriend.file.service.FileService;
import tf.tailfriend.global.service.StorageService;
import tf.tailfriend.global.service.StorageServiceException;
import tf.tailfriend.pet.entity.Pet;
import tf.tailfriend.pet.entity.PetPhoto;
import tf.tailfriend.pet.entity.PetType;
import tf.tailfriend.pet.repository.PetPhotoDao;
import tf.tailfriend.pet.repository.PetDao;
import tf.tailfriend.pet.repository.PetTypeDao;
import tf.tailfriend.petsta.entity.PetstaBookmark;
import tf.tailfriend.petsta.entity.PetstaPost;
import tf.tailfriend.petsitter.repository.PetSitterDao;
import tf.tailfriend.user.entity.SnsType;
import tf.tailfriend.user.entity.User;
import tf.tailfriend.user.entity.UserFollow;
import tf.tailfriend.user.entity.dto.*;
import tf.tailfriend.user.repository.SnsTypeDao;
import tf.tailfriend.user.repository.UserDao;
import tf.tailfriend.user.repository.UserFollowDao;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserDao userDao;
    private final PetSitterDao petSitterDao;
    private final FileDao fileDao;
    private final PetDao petDao;
    private final SnsTypeDao snsTypeDao;
    private final PetTypeDao petTypeDao;
    private final PetPhotoDao petPhotoDao;
    private final UserFollowDao userFollowDao;
    private final FileService fileService;
    private final StorageService storageService;

    // ✅ 이메일로 userId 반환
    public Integer getUserIdBySnsAccountId(String snsAccountId) {
        return userDao.findBySnsAccountId(snsAccountId)
                .map(User::getId)
                .orElse(null);
    }



    @Transactional
    public User registerUser(RegisterUserDto dto, List<MultipartFile> images) {
        // 1. SNS 타입 조회
        SnsType snsType = snsTypeDao.findById(dto.getSnsTypeId())
                .orElseThrow(() -> new RuntimeException("SNS 타입 없음"));

        // 2. 기본 프로필 파일
        File defaultFile = fileService.getOrDefault(dto.getFileId());

        // 3. 유저 저장
        User user = User.builder()
                .nickname(dto.getNickname())
                .snsAccountId(dto.getSnsAccountId())
                .snsType(snsType)
                .file(defaultFile)
                .build();

        userDao.save(user);
        userDao.flush(); // ✅ 즉시 DB 반영해서 user.id 보장

        // 4. 펫 + 사진 등록
        for (RegisterPetDto petDto : dto.getPets()) {
            PetType petType = petTypeDao.findById(petDto.getPetTypeId())
                    .orElseThrow(() -> new RuntimeException("펫 타입 없음"));

            Pet pet = Pet.builder()
                    .user(user)
                    .petType(petType)
                    .name(petDto.getName())
                    .gender(petDto.getGender())
                    .birth(petDto.getBirth())
                    .weight(petDto.getWeight())
                    .info(petDto.getInfo())
                    .neutered(petDto.isNeutered())
                    .activityStatus(petDto.getActivityStatus())
                    .build();

            petDao.save(pet);

                int imageIndex = 0;

                for (RegisterPetPhotoDto photoDto : petDto.getPhotos()) {
                    if (imageIndex >= images.size()) break;

                    MultipartFile image = images.get(imageIndex++);
                    File file = fileService.save(image.getOriginalFilename(), "pet", photoDto.getType());

                    try (InputStream is = image.getInputStream()) {
                        storageService.upload(file.getPath(), is);
                    } catch (IOException | StorageServiceException e) {
                        throw new RuntimeException("파일 업로드 실패: " + e.getMessage(), e);
                    }

                PetPhoto petPhoto = PetPhoto.builder()
                        .id(new PetPhoto.PetPhotoId(file.getId(), pet.getId()))
                        .file(file)
                        .pet(pet)
                        .thumbnail(photoDto.isThumbnail())
                        .build();

                petPhotoDao.save(petPhoto);
            }
        }

        return user;
    }
    /**
     * 회원의 마이페이지 정보를 조회합니다.
     *
     * @param userId 조회할 회원의 ID
     * @return 회원 정보와 반려동물 정보가 포함된 MyPageResponseDto
     */
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

    /**
     * 회원의 닉네임을 업데이트합니다.
     *
     * @param userId      수정할 회원의 ID
     * @param newNickname 새로운 닉네임
     * @return 업데이트된 닉네임
     */
    @Transactional
    public String updateNickname(Integer userId, String newNickname) {
        // 1. 닉네임 유효성 검사
        if (newNickname == null || newNickname.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임은 비어있을 수 없습니다.");
        }

        // 2. 닉네임 길이 제한 (예시: 2-20자)
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

        // 5. 닉네임 업데이트 (instanceof 검사 제거)
        if (user != null) { // null 검사만 유지
            user.updateNickname(newNickname);
        } else {
            throw new UnsupportedOperationException("닉네임 업데이트를 할 수 없습니다.");
        }

        // 6. 저장 및 반환
        user.updateNickname(newNickname);
        userDao.save(user);
        return newNickname;
    }

    /**
     * 회원 프로필 이미지를 업데이트합니다.
     *
     * @param userId 수정할 회원의 ID
     * @param fileId 새 프로필 이미지 파일 ID
     * @return 업데이트된 이미지 URL
     */
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

    /**
     * 회원을 탈퇴시킵니다.
     *
     * @param userId 탈퇴할 회원의 ID
     */
    @Transactional
    public void withdrawMember(Integer userId) {
        // 1. 회원 조회
        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다: " + userId));

        // 2. 펫시터 정보가 있다면 함께 삭제
        petSitterDao.findById(userId).ifPresent(petSitterDao::delete);

        // 3. 회원 삭제
        userDao.delete(user);
    }

    /**
     * Pet 엔티티를 PetResponseDto로 변환합니다.
     */
    private PetResponseDto convertToPetDto(Pet pet) {
        // 1. 반려동물 썸네일 이미지 URL 찾기
        String petProfileImageUrl = pet.getPhotos().stream()
                .filter(PetPhoto::isThumbnail)  // 썸네일로 설정된 사진 필터링
                .findFirst()
                .or(() -> pet.getPhotos().stream().findFirst())
                .map(photo -> storageService.generatePresignedUrl(photo.getFile().getPath()))
                .orElse(null);  // 썸네일이 없으면 null

        // 2. PetResponseDto 생성 및 반환
        return PetResponseDto.builder()
                .id(pet.getId())
                .name(pet.getName())
                .type(pet.getPetType().getName())  // ID가 아닌 이름으로 반환
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
        } else {
            UserFollow newFollow = UserFollow.of(followerUser, followedUser); // << 깔끔
            userFollowDao.save(newFollow);
        }
    }
}
