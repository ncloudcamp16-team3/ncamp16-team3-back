package tf.tailfriend.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tf.tailfriend.file.entity.File;
import tf.tailfriend.file.repository.FileRepository;
import tf.tailfriend.pet.entity.Pet;
import tf.tailfriend.pet.entity.PetPhoto;
import tf.tailfriend.pet.entity.PetType;
import tf.tailfriend.pet.repository.PetPhotoRepository;
import tf.tailfriend.pet.repository.PetRepository;
import tf.tailfriend.pet.repository.PetTypeRepository;
import tf.tailfriend.user.entity.SnsType;
import tf.tailfriend.user.entity.User;
import tf.tailfriend.user.entity.dto.PetPhotoDto;
import tf.tailfriend.user.entity.dto.PetRegisterDto;
import tf.tailfriend.user.entity.dto.UserRegisterDto;
import tf.tailfriend.user.repository.SnsTypeRepository;
import tf.tailfriend.user.repository.UserRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PetRepository petsRepository;
    private final SnsTypeRepository snsTypeRepository;
    private final PetTypeRepository petTypesRepository;
    private final PetPhotoRepository petPhotosRepository;
    private final FileRepository fileRepository;

    // ✅ 이메일로 userId 반환
    public Integer getUserIdBySnsAccountId(String snsAccountId) {
        return userRepository.findBySnsAccountId(snsAccountId)
                .map(User::getId)
                .orElse(null);
    }

    @Transactional
    public User registerUser(UserRegisterDto dto) {
        // 1. SNS 타입 조회
        SnsType snsType = snsTypeRepository.findById(dto.getSnsTypeId())
                .orElseThrow(() -> new RuntimeException("SNS 타입 없음"));

        // 2. 기본 프로필 파일
        File defaultFile = fileRepository.findById(
                dto.getFileId() != null ? dto.getFileId() : 1
        ).orElseThrow(() -> new RuntimeException("기본 파일 없음"));

        // 3. 유저 저장
        User user = User.builder()
                .nickname(dto.getNickname())
                .snsAccountId(dto.getSnsAccountId())
                .snsType(snsType)
                .file(defaultFile)
                .build();

        userRepository.save(user);
        userRepository.flush(); // ✅ 즉시 DB 반영해서 user.id 보장

        // 4. 펫 + 사진 등록
        for (PetRegisterDto petDto : dto.getPets()) {
            PetType petType = petTypesRepository.findById(petDto.getPetTypeId())
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

            petsRepository.save(pet);

            for (PetPhotoDto photoDto : petDto.getPhotos()) {
                File file = File.builder()
                        .type(photoDto.getType())
                        .path(photoDto.getPath())
                        .uuid(photoDto.getUuid() != null ? photoDto.getUuid() : UUID.randomUUID().toString())
                        .build();

                fileRepository.save(file);

                PetPhoto petPhoto = PetPhoto.builder()
                        .id(new PetPhoto.PetPhotoId(file.getId(), pet.getId()))
                        .file(file)
                        .pet(pet)
                        .thumbnail(photoDto.isThumbnail())
                        .build();

                petPhotosRepository.save(petPhoto);
            }
        }

        return user;
    }


}
