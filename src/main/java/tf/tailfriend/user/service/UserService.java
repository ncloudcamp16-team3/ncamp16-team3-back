package tf.tailfriend.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tf.tailfriend.file.entity.File;
import tf.tailfriend.file.service.FileService;
import tf.tailfriend.pet.entity.Pet;
import tf.tailfriend.pet.entity.PetPhoto;
import tf.tailfriend.pet.entity.PetType;
import tf.tailfriend.pet.repository.PetPhotoDao;
import tf.tailfriend.pet.repository.PetDao;
import tf.tailfriend.pet.repository.PetTypeDao;
import tf.tailfriend.user.entity.SnsType;
import tf.tailfriend.user.entity.User;
import tf.tailfriend.user.entity.dto.PetPhotoDto;
import tf.tailfriend.user.entity.dto.PetRegisterDto;
import tf.tailfriend.user.entity.dto.UserRegisterDto;
import tf.tailfriend.user.repository.SnsTypeDao;
import tf.tailfriend.user.repository.UserDao;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserDao userDao;
    private final PetDao petDao;
    private final SnsTypeDao snsTypeDao;
    private final PetTypeDao petTypeDao;
    private final PetPhotoDao petPhotoDao;
    private final FileService fileService;

    // ✅ 이메일로 userId 반환
    public Integer getUserIdBySnsAccountId(String snsAccountId) {
        return userDao.findBySnsAccountId(snsAccountId)
                .map(User::getId)
                .orElse(null);
    }



    @Transactional
    public User registerUser(UserRegisterDto dto) {
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
        for (PetRegisterDto petDto : dto.getPets()) {
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

            for (PetPhotoDto photoDto : petDto.getPhotos()) {
                File file = fileService.save(
                        photoDto.getOriginName(),
                        "pet", // 폴더명
                        photoDto.getType()
                );

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


}
