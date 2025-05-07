package tf.tailfriend.pet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tf.tailfriend.file.entity.File;
import tf.tailfriend.file.repository.FileDao;
import tf.tailfriend.file.service.FileService;
import tf.tailfriend.global.entity.Dong;
import tf.tailfriend.global.repository.DongDao;
import tf.tailfriend.global.service.NCPObjectStorageService;
import tf.tailfriend.global.service.StorageService;
import tf.tailfriend.global.service.StorageServiceException;
import tf.tailfriend.pet.entity.Pet;
import tf.tailfriend.pet.entity.PetPhoto;
import tf.tailfriend.pet.entity.PetType;
import tf.tailfriend.pet.entity.dto.PetDetailResponseDto;
import tf.tailfriend.pet.entity.dto.PetFriendDto;
import tf.tailfriend.pet.entity.dto.PetPhotoDto;
import tf.tailfriend.pet.entity.dto.PetRequestDto;
import tf.tailfriend.pet.exception.FoundDongException;
import tf.tailfriend.pet.exception.FoundFileException;
import tf.tailfriend.pet.exception.FoundPetException;
import tf.tailfriend.pet.exception.NoneActivityStatusException;
import tf.tailfriend.pet.repository.PetDao;
import tf.tailfriend.pet.repository.PetPhotoDao;
import tf.tailfriend.pet.repository.PetTypeDao;
import tf.tailfriend.user.distance.Distance;
import tf.tailfriend.user.entity.User;
import tf.tailfriend.user.repository.UserDao;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetDao petDao;
    private final PetTypeDao petTypeDao;
    private final UserDao userDao;
    private final FileDao fileDao;
    private final FileService fileService;
    private final PetPhotoDao petPhotoDao;
    private final DongDao dongDao;
    private final StorageService storageService;

    //반려동물 상세조회
    @Transactional(readOnly = true)
    public PetDetailResponseDto getPetDetail(Integer petId) {
        Pet pet = petDao.findById(petId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 반려동물입니다: " + petId));

        PetDetailResponseDto petResponse = makePetDetailResponseDto(pet);
        setPublicUrls(petResponse.getPhotos());
        System.out.println("결과값: " + petResponse.getPhotos());
        return petResponse;
    }


    @Transactional(readOnly = true)
    public List<PetDetailResponseDto> getMyPets(Integer userId) {
        List<Pet> myPets = petDao.findByUserId(userId);

        List<PetDetailResponseDto> myPetsDto = new ArrayList<>();
        for (Pet pet : myPets) {
            myPetsDto.add(makePetDetailResponseDto(pet));
        }

        return myPetsDto;
    }

    //반려동물 상세정보 반환 dto생성
    private PetDetailResponseDto makePetDetailResponseDto(Pet pet) {
        List<PetPhotoDto> photoDtos = pet.getPhotos().stream()
                .map(photo -> PetPhotoDto.builder()
                        .id(photo.getFile().getId())
                        .path(photo.getFile().getPath())
                        .thumbnail(photo.isThumbnail())
                        .build())
                .collect(Collectors.toList());

        setPublicUrls(photoDtos);

        return PetDetailResponseDto.builder()
                .ownerId(pet.getUser().getId())
                .id(pet.getId())
                .name(pet.getName())
                .type(mapEnglishToKoreanPetType(pet.getPetType().getName()))
                .birthDate(pet.getBirth())
                .gender(pet.getGender())
                .isNeutered(pet.getNeutered())
                .weight(pet.getWeight())
                .introduction(pet.getInfo())
                .photos(photoDtos)
                .activityStatus(pet.getActivityStatus().toString())
                .build();
    }

    @Transactional(readOnly = true)
    public Page<PetFriendDto> getFriends(String activityStatus, String dongName,
                                         String distance, int page, int size, double latitude, double longitude, Integer myId) {

        if (Pet.ActivityStatus.valueOf(activityStatus) == Pet.ActivityStatus.NONE) {
            throw new NoneActivityStatusException();
        }

        Pageable pageable = PageRequest.of(page, size);
        List<String> dongs = getNearbyDongs(dongName, Distance.valueOf(distance).getValue());

        Page<PetFriendDto> friends = petDao.findByDongNamesAndActivityStatus(
                dongs, activityStatus, latitude, longitude, pageable, myId);

        for (PetFriendDto item : friends.getContent()) {
            List<PetPhotoDto> photos = petPhotoDao.findByPetId(item.getId());
            item.setPhotos(photos);
        }

        for (PetFriendDto friend : friends.getContent()) {
            setPresignedUrl(friend.getPhotos());
        }

        return friends;
    }

    private List<String> getNearbyDongs(String name, int count) {
        Dong currentDong = dongDao.findByName(name)
                .orElseThrow(() -> new FoundDongException());

        return dongDao.findNearbyDongs(
                currentDong.getLatitude(),
                currentDong.getLongitude(),
                count
        );
    }

    //NCP파일 접근url생성
    private void setPresignedUrl(List<PetPhotoDto> photoDtos) {
        if (photoDtos.isEmpty()) {
            File defaultImgFile = fileDao.findById(1)
                    .orElseThrow(() -> new FoundFileException());

            PetPhotoDto defaultPhotoDto = PetPhotoDto.builder()
                    .id(defaultImgFile.getId())
                    .path(storageService.generatePresignedUrl(defaultImgFile.getPath()))
                    .thumbnail(true)
                    .build();

            photoDtos.add(defaultPhotoDto);
        } else {
            for (PetPhotoDto petPhotoDto : photoDtos) {
                petPhotoDto.setPath(storageService.generatePresignedUrl(petPhotoDto.getPath()));
            }
        }
    }

    private void setPublicUrls(List<PetPhotoDto> photoDtos) {
        if (photoDtos.isEmpty()) {
            File defaultImgFile = fileDao.findById(1)
                    .orElseThrow(() -> new FoundFileException());

            System.out.println("뭐가나오는거"+defaultImgFile.getPath());

            PetPhotoDto defaultPhotoDto = PetPhotoDto.builder()
                    .id(defaultImgFile.getId())
                    .path(fileService.getFullUrl(defaultImgFile.getPath()))
                    .thumbnail(true)
                    .build();

            photoDtos.add(defaultPhotoDto);
        } else {
            for (PetPhotoDto dto : photoDtos) {
                dto.setPath(fileService.getFullUrl(dto.getPath()));
            }
        }
    }


    //수정
    @Transactional
    public void updatePet(Integer userId, Integer petId, PetRequestDto petRequestDto,
                          List<MultipartFile> images, MultipartFile existingPhotos) {
        // 1. 유저 확인
        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다: " + userId));

        // 2. 반려동물 조회
        Pet pet = petDao.findById(petId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 반려동물입니다: " + petId));

        // 3. 권한 확인
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

        // 6. 기존 사진 처리
        if (existingPhotos != null && !existingPhotos.isEmpty()) {
            try {
                String existingPhotosJson = new String(existingPhotos.getBytes(), StandardCharsets.UTF_8);
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> photoInfo = objectMapper.readValue(existingPhotosJson, Map.class);

                // 삭제된 이미지 처리
                List<Integer> deleted = (List<Integer>) photoInfo.get("deleted");
                if (deleted != null && !deleted.isEmpty()) {
                    for (Integer photoId : deleted) {
                        pet.getPhotos().removeIf(photo -> photo.getFile().getId().equals(photoId));
                    }
                }

                // 모든 기존 이미지의 썸네일 상태 초기화
                pet.getPhotos().forEach(photo -> photo.setThumbnail(false));

                // 기존 이미지의 썸네일 설정 업데이트
                List<Map<String, Object>> existing = (List<Map<String, Object>>) photoInfo.get("existing");
                if (existing != null) {
                    for (Map<String, Object> photoData : existing) {
                        Integer photoId = (Integer) photoData.get("id");
                        Boolean thumbnail = (Boolean) photoData.get("thumbnail");

                        pet.getPhotos().stream()
                                .filter(photo -> photo.getFile().getId().equals(photoId))
                                .findFirst()
                                .ifPresent(photo -> photo.setThumbnail(thumbnail));
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("기존 사진 정보 처리 실패: " + e.getMessage(), e);
            }
        }

        // 7. 새로운 이미지 처리
        if (images != null && !images.isEmpty()) {
            for (int i = 0; i < images.size(); i++) {
                MultipartFile image = images.get(i);
                if (image != null && !image.isEmpty()) {
                    File file = fileService.save(image.getOriginalFilename(), "pet", File.FileType.PHOTO);

                    try (InputStream is = image.getInputStream()) {
                        storageService.openUpload(file.getPath(), is);
                    } catch (IOException | StorageServiceException e) {
                        throw new RuntimeException("파일 업로드 실패: " + e.getMessage(), e);
                    }

                    // 새로 추가되는 이미지의 썸네일 설정은 기본 false
                    pet.addPhoto(file, false);
                }
            }
        }

        // 8. 최종 썸네일 확인 및 설정
        boolean hasThumbnail = pet.getPhotos().stream()
                .anyMatch(PetPhoto::isThumbnail);

        if (!hasThumbnail && !pet.getPhotos().isEmpty()) {
            // 썸네일이 없으면 첫 번째 사진을 썸네일로 설정
            pet.getPhotos().get(0).setThumbnail(true);
        }
    }

    @Transactional
    public void MyupdatePet(PetDetailResponseDto petDetailResponseDto) {
        Pet petEntity = petDao.findById(petDetailResponseDto.getId())
                .orElseThrow(() -> new FoundPetException());

        Pet updatedPet = petEntity.toBuilder()
                .name(petDetailResponseDto.getName())
                .gender(petDetailResponseDto.getGender())
                .neutered(petDetailResponseDto.getIsNeutered())
                .weight(petDetailResponseDto.getWeight())
                .info(petDetailResponseDto.getIntroduction())
                .activityStatus(Pet.ActivityStatus.valueOf(petDetailResponseDto.getActivityStatus()))
                .build();

        petDao.save(updatedPet);
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
            int mainIndex = (petRequestDto.getMainPhotoIndex() != null)
                    ? petRequestDto.getMainPhotoIndex() : 0;

            // 유효한 인덱스인지 확인
            if (mainIndex >= images.size()) {
                mainIndex = 0;
            }

            // 모든 이미지 처리
            for (int i = 0; i < images.size(); i++) {
                MultipartFile image = images.get(i);
                if (image != null && !image.isEmpty()) {
                    File file = fileService.save(image.getOriginalFilename(), "pet", File.FileType.PHOTO);

                    try (InputStream is = image.getInputStream()) {
                        storageService.openUpload(file.getPath(), is);
                    } catch (IOException | StorageServiceException e) {
                        throw new RuntimeException("파일 업로드 실패: " + e.getMessage(), e);
                    }

                    boolean isThumbnail = (i == mainIndex);

                    savedPet.addPhoto(file, isThumbnail);
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