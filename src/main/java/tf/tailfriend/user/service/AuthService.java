package tf.tailfriend.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tf.tailfriend.file.entity.File;
import tf.tailfriend.file.service.FileService;
import tf.tailfriend.global.config.JwtTokenProvider;
import tf.tailfriend.global.service.StorageService;
import tf.tailfriend.global.service.StorageServiceException;
import tf.tailfriend.pet.entity.Pet;
import tf.tailfriend.pet.entity.PetPhoto;
import tf.tailfriend.pet.entity.PetType;
import tf.tailfriend.pet.repository.PetDao;
import tf.tailfriend.pet.repository.PetPhotoDao;
import tf.tailfriend.pet.repository.PetTypeDao;
import tf.tailfriend.user.entity.SnsType;
import tf.tailfriend.user.entity.User;
import tf.tailfriend.user.entity.dto.*;
import tf.tailfriend.user.exception.PetTypeException;
import tf.tailfriend.user.exception.SnsTypeException;
import tf.tailfriend.user.exception.UserException;
import tf.tailfriend.user.repository.SnsTypeDao;
import tf.tailfriend.user.repository.UserDao;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;



@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserDao userDao;
    private final PetDao petDao;
    private final SnsTypeDao snsTypeDao;
    private final PetTypeDao petTypeDao;
    private final PetPhotoDao petPhotoDao;

    private final FileService fileService;
    private final StorageService storageService;


    public UserInfoDto getUserInfoById(Integer userId) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new UserException());

        String fileUrl = storageService.generatePresignedUrl(user.getFile().getPath());


        return new UserInfoDto(
                user.getId(),
                user.getNickname(),
                user.getAddress(),
                user.getDongName(),
                user.getLatitude(),
                user.getLongitude(),
                fileUrl,
                user.getDistance()
        );
    }

    public Integer getUserIdBySnsAccountIdAndSnsTypeId(String snsAccountId, Integer snsTypeId) {
        return userDao.findBySnsAccountIdAndSnsTypeIdAndDeletedFalse(snsAccountId, snsTypeId)
                .map(User::getId)
                .orElse(null);
    }


    @Transactional
    public User registerUser(RegisterUserDto dto, List<MultipartFile> images) {
        // 1. SNS 타입 조회
        SnsType snsType = snsTypeDao.findById(dto.getSnsTypeId())
                .orElseThrow(() -> new SnsTypeException());

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

        // 4. 펫 + 사진 등록
        for (RegisterPetDto petDto : dto.getPets()) {
            PetType petType = petTypeDao.findById(petDto.getPetTypeId())
                    .orElseThrow(() -> new PetTypeException());

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
                    storageService.openUpload(file.getPath(), is);
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

    public boolean isNicknameExists(String nickname) {
        return userDao.existsByNickname(nickname);
    }


}
