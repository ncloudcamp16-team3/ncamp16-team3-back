package tf.tailfriend.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tf.tailfriend.petsitter.dto.PetSitterResponseDto;
import tf.tailfriend.petsitter.entity.PetSitter;
import tf.tailfriend.petsitter.service.PetSitterService;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminPetSitterController {

    private final PetSitterService petSitterService;

    @GetMapping("/petsitter/list")
    public ResponseEntity<?> PetSitterList(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").descending());

        Page<PetSitterResponseDto> petSitters = petSitterService.findAll(pageRequest);

        return ResponseEntity.ok(petSitters);
    }

    @GetMapping("/petsitter/{id}")
    public ResponseEntity<?> getPetSitterById(@PathVariable Integer id) {
        PetSitterResponseDto petSitter = petSitterService.findById(id);
        log.info("petSitter: {}", petSitter);
        return ResponseEntity.ok(petSitter);
    }
}
