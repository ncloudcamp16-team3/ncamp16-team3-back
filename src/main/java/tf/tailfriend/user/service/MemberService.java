package tf.tailfriend.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tf.tailfriend.file.entity.File;
import tf.tailfriend.file.repository.FileRepository;
import tf.tailfriend.pet.entity.Pet;
import tf.tailfriend.pet.entity.PetPhoto;
import tf.tailfriend.user.entity.User;
import tf.tailfriend.user.entity.dto.MypageResponseDto;
import tf.tailfriend.user.entity.dto.PetResponseDto;
import tf.tailfriend.user.repository.UserRepository;
import tf.tailfriend.petsitter.repository.PetSitterRepository;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final UserRepository userRepository;
    private final PetSitterRepository petSitterRepository;
    private final FileRepository fileRepository;

    /**

     * @param userId 조회할 회원의 ID
     * @return 회원 정보와 반려동물 정보가 포함된 MyPageResponseDto
     */
    public MypageResponseDto getMemberInfo(Integer userId) {
        // 1. 회원 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다: " + userId));

        // 2. 반려동물 정보 변환
        List<PetResponseDto> petDtos = user.getPet().stream()
                .map(this::convertToPetDto)
                .collect(Collectors.toList());

        // 3. 펫시터 여부 확인
        boolean isSitter = petSitterRepository.existsById(userId);

        // 4. 응답 DTO 생성 및 반환
        return MypageResponseDto.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getFile().getPath())
                .pets(petDtos)
                .isSitter(isSitter)
                .build();
    }

    /**

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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다: " + userId));

        // 4. 닉네임 중복 검사
        userRepository.findByNickname(newNickname)
                .filter(u -> !u.getId().equals(userId)) // 자기 자신은 제외
                .ifPresent(u -> {
                    throw new IllegalArgumentException("이미 사용 중인 닉네임입니다: " + newNickname);
                });

        // 5. 닉네임 업데이트
        if (user instanceof User) {
            user.updateNickname(newNickname);
        } else {
            // user.setNickname(newNickname);
            throw new UnsupportedOperationException("닉네임 업데이트 메서드가 구현되어 있지 않습니다.");
        }

        // 6. 저장 및 반환
        userRepository.save(user);
        return newNickname;
    }

    /**

     * @param userId 수정할 회원의 ID
     * @param fileId 새 프로필 이미지 파일 ID
     * @return 업데이트된 이미지 URL
     */
    @Transactional
    public String updateProfileImage(Integer userId, Integer fileId) {
        // 1. 회원 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다: " + userId));

        // 2. 파일 조회
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 파일입니다: " + fileId));

        // 3. 프로필 이미지 업데이트
        user.updateProfileImage(file);

        // 4. 저장 및 URL 반환
        userRepository.save(user);
        return file.getPath();
    }

    /**
     * @param userId 탈퇴할 회원의 ID
     */
    @Transactional
    public void withdrawMember(Integer userId) {
        // 1. 회원 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다: " + userId));

        // 2. 펫시터 정보가 있다면 함께 삭제
        petSitterRepository.findById(userId).ifPresent(petSitterRepository::delete);

        // 3. 회원 삭제
        // 참고: CASCADE 설정에 따라 연관된 엔티티(반려동물 등)도 함께 삭제될 수 있음
        userRepository.delete(user);
    }

    /**
     * Pet 엔티티를 PetResponseDto로 변환합니다.
     */
    private PetResponseDto convertToPetDto(Pet pet) {
        // 1. 반려동물 썸네일 이미지 URL 찾기
        String petProfileImageUrl = pet.getPhotos().stream()
                .filter(PetPhoto::isThumbnail)  // 썸네일로 설정된 사진 필터링
                .findFirst()
                .map(photo -> photo.getFile().getPath())
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
}