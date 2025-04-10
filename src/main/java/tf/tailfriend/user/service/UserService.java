package tf.tailfriend.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tf.tailfriend.user.entity.*;
import tf.tailfriend.user.entity.dto.NewDto;
import tf.tailfriend.user.entity.dto.PetPhotoDto;
import tf.tailfriend.user.entity.dto.PetRegisterDto;
import tf.tailfriend.user.entity.dto.UserRegisterDto;
import tf.tailfriend.user.repository.*;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UsersRepository usersRepository;
    private final PetsRepository petsRepository;
    private final SnsTypeRepository snsTypeRepository;
    private final PetTypesRepository petTypesRepository;
    private final PetPhotosRepository petPhotosRepository;
    private final FilesRepository filesRepository;

    // ✅ 이메일로 userId 반환
    public Integer getUserIdBySnsAccountId(String snsAccountId) {
        return usersRepository.findBySnsAccountId(snsAccountId)
                .map(Users::getId)
                .orElse(null);
    }

    @Transactional
    public void registerUser(UserRegisterDto dto) {
        // 1. SNS 타입 조회
        SnsTypes snsType = snsTypeRepository.findById(dto.getSnsTypeId())
                .orElseThrow(() -> new RuntimeException("SNS 타입 없음"));

        // 2. 기본 프로필 파일
        Files defaultFile = filesRepository.findById(
                dto.getFileId() != null ? dto.getFileId() : 1
        ).orElseThrow(() -> new RuntimeException("기본 파일 없음"));

        // 3. 유저 저장
        Users user = Users.builder()
                .nickname(dto.getNickname())
                .snsAccountId(dto.getSnsAccountId())
                .snsType(snsType)
                .file(defaultFile)
                .build();

        usersRepository.save(user);

        // 4. 펫 + 사진 등록
        for (PetRegisterDto petDto : dto.getPets()) {
            PetTypes petType = petTypesRepository.findById(petDto.getPetTypeId())
                    .orElseThrow(() -> new RuntimeException("펫 타입 없음"));

            Pets pet = Pets.builder()
                    .user(user)
                    .petType(petType)
                    .name(petDto.getName())
                    .gender(petDto.getGender())
                    .birth(petDto.getBirth())
                    .weight(petDto.getWeight())
                    .info(petDto.getInfo())
                    .neutured(petDto.isNeutured())
                    .activityStatus(petDto.getActivityStatus())
                    .build();

            petsRepository.save(pet);

            for (PetPhotoDto photoDto : petDto.getPhotos()) {
                Files file = Files.builder()
                        .type(photoDto.getType())
                        .path(photoDto.getPath())
                        .uuid(photoDto.getUuid() != null ? photoDto.getUuid() : UUID.randomUUID().toString())
                        .build();

                filesRepository.save(file);

                PetPhotos petPhoto = PetPhotos.builder()
                        .id(new PetPhotosId(file.getId(), pet.getId()))
                        .file(file)
                        .pet(pet)
                        .thumbnail(photoDto.isThumbnail())
                        .build();

                petPhotosRepository.save(petPhoto);
            }
        }
    }

}
