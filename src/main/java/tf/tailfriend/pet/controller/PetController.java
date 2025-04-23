package tf.tailfriend.pet.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tf.tailfriend.global.config.UserPrincipal;
import tf.tailfriend.global.response.CustomResponse;
import tf.tailfriend.pet.entity.dto.PetRequestDto;
import tf.tailfriend.pet.service.PetService;

import java.util.List;

@RestController
@RequestMapping("/api/pet")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

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
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        if (userPrincipal == null) {
            return ResponseEntity.status(401)
                    .body(new CustomResponse("로그인이 필요합니다.", null));
        }

        try {
            Integer userId = userPrincipal.getUserId();
            petService.updatePet(userId, petId, petRequestDto, images);

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
}