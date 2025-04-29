package tf.tailfriend.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tf.tailfriend.petsitter.dto.PetSitterResponseDto;
import tf.tailfriend.petsitter.service.PetSitterService;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminPetSitterController {

    private final PetSitterService petSitterService;

    @GetMapping("/petsitter/list")
    public ResponseEntity<?> petSitterList(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String searchField
    ) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").descending());
        Page<PetSitterResponseDto> petSitters;

        if (searchTerm != null && !searchTerm.isEmpty()) {
            // 검색어가 있을 경우
            petSitters = petSitterService.findBySearchCriteria(searchTerm, searchField, pageRequest);
        } else {
            // 검색어가 없을 경우 모든 펫시터 불러오기
            petSitters = petSitterService.findApprovePetSitter(pageRequest);
        }

        return ResponseEntity.ok(petSitters);
    }

    @GetMapping("/petsitter/{id}")
    public ResponseEntity<?> getPetSitterById(@PathVariable Integer id) {
        PetSitterResponseDto petSitter = petSitterService.findById(id);
//        log.info("petSitter: {}", petSitter);
        return ResponseEntity.ok(petSitter);
    }

    @GetMapping("/petsitter/pending")
    public ResponseEntity<?> noneList(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").descending());

        Page<PetSitterResponseDto> petSitters = petSitterService.findNonePetSitter(pageRequest);

        return ResponseEntity.ok(petSitters);
    }

    @GetMapping("/petsitter/pending/{id}")
    public ResponseEntity<?> getPendingPetSitterById(@PathVariable Integer id) {
        PetSitterResponseDto petSitter = petSitterService.findById(id);
        return ResponseEntity.ok(petSitter);
    }

    @PostMapping("/petsitter/pending/{id}/approve")
    public ResponseEntity<?> approvePetSitter(@PathVariable Integer id) {
        PetSitterResponseDto petSitter = petSitterService.approvePetSitter(id);
        return ResponseEntity.ok(petSitter);
    }

    @PostMapping("/petsitter/pending/{id}/pending")
    public ResponseEntity<?> pendingPetSitter(@PathVariable Integer id) {
        PetSitterResponseDto petSitter = petSitterService.pendingPetSitter(id);
        return ResponseEntity.ok(petSitter);
    }

    @PostMapping("/petsitter/pending/{id}/delete")
    public ResponseEntity<?> deletePetSitter(@PathVariable Integer id) {
        PetSitterResponseDto petSitter = petSitterService.deletePetSitter(id);
        return ResponseEntity.ok(petSitter);
    }


    @PostMapping("/petsitter/approve/{id}")
    public ResponseEntity<?> approve(@PathVariable Integer id) {
        try {
            PetSitterResponseDto result = petSitterService.approvePetSitter(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("승인 실패: " + e.getMessage());
        }
    }

    @PostMapping("/petsitter/pending/{id}")
    public ResponseEntity<?> pending(@PathVariable Integer id) {
        try {
            PetSitterResponseDto result = petSitterService.pendingPetSitter(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("보류 처리 실패: " + e.getMessage());
        }
    }

    @PostMapping("/petsitter/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            PetSitterResponseDto result = petSitterService.deletePetSitter(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("삭제 처리 실패: " + e.getMessage());
        }
    }

}
