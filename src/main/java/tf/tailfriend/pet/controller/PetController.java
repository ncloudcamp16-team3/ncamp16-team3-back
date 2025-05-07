package tf.tailfriend.pet.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tf.tailfriend.global.config.UserPrincipal;
import tf.tailfriend.global.response.CustomResponse;
import tf.tailfriend.pet.entity.dto.PetDetailResponseDto;
import tf.tailfriend.pet.entity.dto.PetRequestDto;
import tf.tailfriend.pet.exception.FoundPetException;
import tf.tailfriend.pet.exception.GetMyPetException;
import tf.tailfriend.pet.exception.UpdatePetException;
import tf.tailfriend.pet.service.PetService;
import tf.tailfriend.pet.entity.dto.FindFriendRequestDto;
import tf.tailfriend.pet.entity.dto.PetFriendDto;
import tf.tailfriend.user.exception.UnauthorizedException;

import java.util.List;
import java.util.Objects;

import static tf.tailfriend.pet.message.SuccessMessage.*;

@RestController
@RequestMapping("/api/pet")
@RequiredArgsConstructor
@Slf4j
public class PetController {

    private final PetService petService;

    //반려동물 상세 정보 조회
    @GetMapping("/{petId}")
    public ResponseEntity<?> getPet(@PathVariable Integer petId) {
        try {
            PetDetailResponseDto pet = petService.getPetDetail(petId);

            return ResponseEntity.ok(new CustomResponse(PET_FOUND_SUCCESS.getMessage(), pet));
        } catch (Exception e) {
            throw new FoundPetException();
        }
    }

    // 반려동물 추가
    @PostMapping("/add")
    public ResponseEntity<CustomResponse> addPet(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestPart("petData") PetRequestDto petRequestDto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        if (userPrincipal == null) {
            return ResponseEntity.status(401)
                    .body(new CustomResponse("로그인이 필요합니다.", null));
        }

        try {
            Integer userId = userPrincipal.getUserId();
            Integer petId = petService.addPet(userId, petRequestDto, images);

            return ResponseEntity.ok(new CustomResponse("반려동물 등록이 완료되었습니다.", petId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(new CustomResponse("반려동물 등록 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }

    // 반려동물 정보 수정
    @PutMapping("/{petId}/update")
    public ResponseEntity<?> updatePet(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Integer petId,
            @RequestPart("petData") PetRequestDto petRequestDto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "existingPhotos", required = false) MultipartFile existingPhotos) {

        if (userPrincipal == null) {
            return ResponseEntity.status(401)
                    .body(new CustomResponse("로그인이 필요합니다.", null));
        }

        try {
            Integer userId = userPrincipal.getUserId();
            petService.updatePet(userId, petId, petRequestDto, images, existingPhotos);

            return ResponseEntity.ok(new CustomResponse("반려동물 정보가 수정되었습니다.", petId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(new CustomResponse("반려동물 정보 수정 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }

    // 반려동물 삭제
    @DeleteMapping("/{petId}")
    public ResponseEntity<?> deletePet(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Integer petId) {

        if (userPrincipal == null) {
            return ResponseEntity.status(401)
                    .body(new CustomResponse("로그인이 필요합니다.", null));
        }

        try {
            Integer userId = userPrincipal.getUserId();
            petService.deletePet(userId, petId);

            return ResponseEntity.ok(new CustomResponse("반려동물 정보가 삭제되었습니다.", null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(new CustomResponse("반려동물 정보 삭제 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }

    @PostMapping("/friends")
    public ResponseEntity<?> getFriendList(@RequestBody FindFriendRequestDto findFriendRequestDTO, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Page<PetFriendDto> petFriends = petService.getFriends(
                findFriendRequestDTO.getActivityStatus(),
                findFriendRequestDTO.getDongName(),
                findFriendRequestDTO.getDistance(),
                findFriendRequestDTO.getPage(),
                findFriendRequestDTO.getSize(),
                findFriendRequestDTO.getLatitude(),
                findFriendRequestDTO.getLongitude(),
                userPrincipal.getUserId()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new CustomResponse(GET_FRIENDS_SUCCESS.getMessage(), petFriends));
    }

    @PostMapping("/my/{userId}")
    public ResponseEntity<?> getMyPets(@PathVariable Integer userId, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            if (!Objects.equals(userId, userPrincipal.getUserId())) {
                throw new UnauthorizedException();
            }
            List<PetDetailResponseDto> myPets = petService.getMyPets(userId);

            log.info("나의 펫 리스트 : {}", myPets);

            return ResponseEntity.ok(new CustomResponse(GET_MY_PETS_SUCCESS.getMessage(), myPets));
        } catch (Exception e) {
            throw new GetMyPetException();
        }
    }

    @PutMapping("/save/{userId}")
    public ResponseEntity<?> savePet(@PathVariable Integer userId, @AuthenticationPrincipal UserPrincipal userPrincipal,
                                     @RequestBody PetDetailResponseDto petDetailResponseDto) {

        if (!Objects.equals(userId, userPrincipal.getUserId())) {
            throw new UnauthorizedException();
        }

        try {
            petService.MyupdatePet(petDetailResponseDto);

            return ResponseEntity.ok(new CustomResponse(UPDATE_PET_SUCCESS.getMessage(), null));
        } catch (Exception e) {
            throw new UpdatePetException();
        }
    }
}