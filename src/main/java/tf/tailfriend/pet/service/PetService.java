package tf.tailfriend.pet.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tf.tailfriend.file.entity.File;
import tf.tailfriend.file.repository.FileDao;
import tf.tailfriend.file.service.FileService;
import tf.tailfriend.global.service.NCPObjectStorageService;
import tf.tailfriend.pet.entity.dto.PetDetailResponseDto;
import tf.tailfriend.pet.entity.dto.PetRequestDto;
import tf.tailfriend.pet.entity.Pet;
import tf.tailfriend.pet.entity.PetType;
import tf.tailfriend.pet.repository.PetDao;
import tf.tailfriend.pet.repository.PetTypeDao;
import tf.tailfriend.petmeeting.dto.PetFriendDTO;
import tf.tailfriend.petmeeting.dto.PetPhotoDTO;
import tf.tailfriend.petmeeting.exception.FindFileException;
import tf.tailfriend.user.entity.User;
import tf.tailfriend.user.repository.UserDao;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetDao petDao;
    private final PetTypeDao petTypeDao;
    private final UserDao userDao;
    private final FileDao fileDao;
    private final FileService fileService;
    private final NCPObjectStorageService ncpObjectStorageService;

    //본인 반려동물 조회
    @Transactional(readOnly = true)
    public PetDetailResponseDto getPetDetail(Integer userId, Integer petId) {
        // 1. 유저 확인
        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다: " + userId));

        // 2. 반려동물 조회
        Pet pet = petDao.findById(petId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 반려동물입니다: " + petId));

        // 3. 권한 확인 (자신의 반려동물만 조회 가능)
        if (!pet.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 반려동물의 정보를 조회할 권한이 없습니다.");
        }

        // 4. 반려동물 엔티티로 dto 생성 후 반환
        return makePetDetailResponseDto(pet);
    }

    //반려동물 상세조회
    @Transactional(readOnly = true)
    public PetDetailResponseDto getFriend(Integer petId) {
        Pet pet = petDao.findById(petId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 반려동물입니다: " + petId));

        return makePetDetailResponseDto(pet);
    }

    //NCP파일 접근 url생성
    private void setPresignedUrl(List<PetDetailResponseDto.PetPhotoDto> photoDtos) {

        if (photoDtos.isEmpty()) {
            File defaultImgFile = fileDao.findById(1)
                    .orElseThrow(() -> new FindFileException());

            PetDetailResponseDto.PetPhotoDto defaultPhotoDto = PetDetailResponseDto.PetPhotoDto.builder()
                    .id(defaultImgFile.getId())
                    .url(ncpObjectStorageService.generatePresignedUrl(defaultImgFile.getPath()))
                    .isThumbnail(true)
                    .build();

            photoDtos.add(defaultPhotoDto);
        } else {
            for (PetDetailResponseDto.PetPhotoDto petPhotoDto : photoDtos) {
                petPhotoDto.setUrl(ncpObjectStorageService.generatePresignedUrl(petPhotoDto.getUrl()));
            }
        }
    }

    //반려동물 상세정보 반환 dto생성
    private PetDetailResponseDto makePetDetailResponseDto(Pet pet) {
        List<PetDetailResponseDto.PetPhotoDto> photoDtos = pet.getPhotos().stream()
                .map(photo -> PetDetailResponseDto.PetPhotoDto.builder()
                        .id(photo.getFile().getId())
                        .url(photo.getFile().getPath())
                        .isThumbnail(photo.isThumbnail())
                        .build())
                .collect(Collectors.toList());

        setPresignedUrl(photoDtos);

        return PetDetailResponseDto.builder()
                .id(pet.getId())
                .name(pet.getName())
                .type(mapEnglishToKoreanPetType(pet.getPetType().getName()))
                .birthDate(pet.getBirth())
                .gender(pet.getGender())
                .isNeutered(pet.getNeutered())
                .weight(pet.getWeight())
                .introduction(pet.getInfo())
                .isFavorite(false)
                .photos(photoDtos)
                .build();
    }

    //수정
    @Transactional
    public void updatePet(Integer userId, Integer petId, PetRequestDto petRequestDto, List<MultipartFile> images) {
        // 1. 유저 확인
        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다: " + userId));

        // 2. 반려동물 조회
        Pet pet = petDao.findById(petId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 반려동물입니다: " + petId));

        // 3. 권한 확인 (자신의 반려동물만 수정 가능)
        if (!pet.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 반려동물의 정보를 수정할 권한이 없습니다.");
        }

        // 4. 반려동물 타입 조회
        PetType petType = getPetTypeByName(petRequestDto.getType());

        // 5. 반려동물 정보 업데이트
        pet.updateInfo(
                petType,
                petRequestDto.getName(),
                petRequestDto.getGender(),
                petRequestDto.getBirthDate(),
                petRequestDto.getWeight(),
                petRequestDto.getIntroduction(),
                petRequestDto.getIsNeutered()
        );

        // 6. 이미지 처리
        if (images != null && !images.isEmpty()) {

            boolean isFirst = true;
            for (MultipartFile image : images) {
                if (image != null && !image.isEmpty()) {
                    // 파일 저장
                    File file = fileService.save(image.getOriginalFilename(), "pet", File.FileType.PHOTO);

                    // S3 업로드 로직 (필요시)

                    // Pet에 사진 추가
                    pet.addPhoto(file, isFirst);
                    isFirst = false;
                }
            }
        }
    }

    //삭제
    @Transactional
    public void deletePet(Integer userId, Integer petId) {
        // 1. 유저 확인
        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다: " + userId));

        // 2. 반려동물 조회
        Pet pet = petDao.findById(petId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 반려동물입니다: " + petId));

        // 3. 권한 확인 (자신의 반려동물만 삭제 가능)
        if (!pet.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 반려동물의 정보를 삭제할 권한이 없습니다.");
        }

        // 4. 반려동물 삭제
        petDao.delete(pet);
    }

    private String mapEnglishToKoreanPetType(String englishType) {
        return switch (englishType) {
            case "DOG" -> "강아지";
            case "CAT" -> "고양이";
            case "HAMSTER" -> "햄스터";
            case "PARROT" -> "앵무새";
            case "FISH" -> "물고기";
            default -> "기타";
        };
    }

    //추가
    @Transactional
    public Integer addPet(Integer userId, PetRequestDto petRequestDto, List<MultipartFile> images) {
        // 1. 유저 조회
        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다: " + userId));

        // 2. 반려동물 타입 조회
        PetType petType = getPetTypeByName(petRequestDto.getType());

        // 3. 반려동물 객체 생성
        Pet pet = Pet.builder()
                .user(user)
                .petType(petType)
                .name(petRequestDto.getName())
                .gender(petRequestDto.getGender())
                .birth(petRequestDto.getBirthDate())
                .weight(petRequestDto.getWeight())
                .info(petRequestDto.getIntroduction())
                .neutered(petRequestDto.getIsNeutered())
                .activityStatus(Pet.ActivityStatus.NONE)
                .build();

        // 4. 반려동물 저장
        Pet savedPet = petDao.save(pet);

        // 5. 이미지 처리
        if (images != null && !images.isEmpty()) {
            boolean isFirst = true;
            for (MultipartFile image : images) {
                if (image != null && !image.isEmpty()) {
                    // 파일 저장
                    File file = fileService.save(image.getOriginalFilename(), "pet", File.FileType.PHOTO);

                    // Pet에 사진 추가 (첫 번째 이미지를 썸네일로 설정)
                    savedPet.addPhoto(file, isFirst);
                    isFirst = false;
                }
            }
        }

        return savedPet.getId();
    }

    private PetType getPetTypeByName(String typeName) {
        // 반려동물 타입 이름으로 조회 (DB에 저장된 영어 이름과 매핑 필요)
        String englishName = mapKoreanToEnglishPetType(typeName);

        return petTypeDao.findByName(englishName)
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 반려동물 타입입니다: " + typeName));
    }

    private String mapKoreanToEnglishPetType(String koreanType) {
        return switch (koreanType) {
            case "강아지" -> "DOG";
            case "고양이" -> "CAT";
            case "햄스터" -> "HAMSTER";
            case "앵무새" -> "PARROT";
            case "물고기" -> "FISH";
            default -> "ETC";
        };
    }
}