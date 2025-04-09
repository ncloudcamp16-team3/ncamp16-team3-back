package tf.tailfriend.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tf.tailfriend.user.entity.*;
import tf.tailfriend.user.entity.dto.UserRegisterDto;
import tf.tailfriend.user.repository.*;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UsersRepository usersRepository;
    private final SnsTypeRepository snsTypeRepository;
    private final PetTypesRepository petTypesRepository;
    private final PetPhotoRepository petPhotoRepository;
    private final FilesRepository filesRepository;

    // ✅ 이메일로 userId 반환
    public Integer getUserIdBySnsAccountId(String snsAccountId) {
        return usersRepository.findBySnsAccountId(snsAccountId)
                .map(Users::getId)
                .orElse(null);
    }

    // ✅ 회원 + 반려동물 + 사진 등록
    @Transactional
    public Users registerUser(UserRegisterDto dto) {
        // 1. SNS 타입 조회
        SnsType snsType = snsTypeRepository.findById(dto.getSnsTypeId())
                .orElseThrow(() -> new RuntimeException("SNS 타입 없음"));

        // 2. 유저 객체 생성
        Users user = Users.builder()
                .nickname(dto.getNickname())
                .snsAccountId(dto.getSnsAccountId())
                .snsType(snsType)
                .address(dto.getAddress())
                .detailAddress(dto.getDetailAddress())
                .dongName(dto.getDongName())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .build();

        // 유저 저장 먼저 (ID 생성)
        usersRepository.save(user);

        // 3. 반려동물 처리
        for (var petDto : dto.getPets()) {
            PetTypes petType = petTypesRepository.findById(petDto.getPetTypeId())
                    .orElseThrow(() -> new RuntimeException("펫 타입 없음"));

            Pets pet = Pets.builder()
                    .owner(user)
                    .petType(petType)
                    .name(petDto.getName())
                    .gender(petDto.getGender())
                    .birth(petDto.getBirth())
                    .weight(petDto.getWeight())
                    .info(petDto.getInfo())
                    .neutured(petDto.isNeutured())
                    .build();

            // 반려동물 먼저 저장해서 ID 확보
            user.getPets().add(pet); // 양방향 관계 유지
        }

        // 반려동물 저장
        usersRepository.save(user); // pets도 cascade 저장되어 id 생성됨

        // 4. petPhoto 처리
        for (Pets pet : user.getPets()) {
            var matchingPetDto = dto.getPets().stream()
                    .filter(p -> p.getName().equals(pet.getName())) // 이름 기준 매칭 (ID 없음)
                    .findFirst();

            if (matchingPetDto.isPresent() && matchingPetDto.get().getPetPhotos() != null) {
                for (var photoDto : matchingPetDto.get().getPetPhotos()) {
                    Files file = filesRepository.findById(photoDto.getFileId())
                            .orElseThrow(() -> new RuntimeException("파일 없음"));

                    PetPhoto photo = PetPhoto.builder()
                            .file(file)
                            .pet(pet)
                            .thumbnail(photoDto.isThumbnail())
                            .id(new PetPhotoId(file.getId(), pet.getId())) // ID 명시적 설정
                            .build();

                    pet.addPhoto(photo); // 양방향 연관관계 유지
                }
            }
        }

        // 최종 저장 (PetPhoto 포함)
        return usersRepository.save(user);
    }
}
