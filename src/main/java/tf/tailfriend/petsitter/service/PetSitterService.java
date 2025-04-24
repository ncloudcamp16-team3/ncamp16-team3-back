package tf.tailfriend.petsitter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tf.tailfriend.global.service.StorageService;
import tf.tailfriend.petsitter.dto.PetSitterResponseDto;
import tf.tailfriend.petsitter.entity.PetSitter;
import tf.tailfriend.petsitter.repository.PetSitterDao;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PetSitterService {

    private final PetSitterDao petSitterDao;
    private final StorageService storageService;

    @Transactional(readOnly = true)
    public Page<PetSitterResponseDto> findAll(Pageable pageable) {
        Page<PetSitter> petSitters = petSitterDao.findAll(pageable);
        return convertToDtoPage(petSitters, pageable);
    }

    @Transactional(readOnly = true)
    public Page<PetSitterResponseDto> findApprovePetSitter(Pageable pageable) {
        Page<PetSitter> petSitters = petSitterDao.findByStatus(PetSitter.PetSitterStatus.APPROVE, pageable);
        return convertToDtoPage(petSitters, pageable);
    }

    @Transactional(readOnly = true)
    public Page<PetSitterResponseDto> findNonePetSitter(Pageable pageable) {
        Page<PetSitter> petSitters = petSitterDao.findByStatus(PetSitter.PetSitterStatus.NONE, pageable);
        return convertToDtoPage(petSitters, pageable);
    }

    @Transactional(readOnly = true)
    public PetSitterResponseDto findById(Integer id) {
        PetSitter petSitter = petSitterDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("펫시터가 존재하지 않습니다 " + id));

        return PetSitterResponseDto.fromEntity(petSitter);
    }

    @Transactional(readOnly = true)
    public Page<PetSitterResponseDto> findBySearchCriteria(String searchTerm, String searchField, Pageable pageable) {
        Page<PetSitter> petSitters;

        switch (searchField) {
            case "nickname":
                petSitters = petSitterDao.findByUserNicknameContainingAndStatusEquals(
                        searchTerm, PetSitter.PetSitterStatus.APPROVE, pageable);
                break;
            case "age":
                petSitters = petSitterDao.findByAgeContainingAndStatusEquals(
                        searchTerm, PetSitter.PetSitterStatus.APPROVE, pageable);
                break;
            case "houseType":
                petSitters = petSitterDao.findByHouseTypeContainingAndStatusEquals(
                        searchTerm, PetSitter.PetSitterStatus.APPROVE, pageable);
                break;
            case "comment":
                petSitters = petSitterDao.findByCommentContainingAndStatusEquals(
                        searchTerm, PetSitter.PetSitterStatus.APPROVE, pageable);
                break;
            default:
                petSitters = petSitterDao.findByStatus(PetSitter.PetSitterStatus.APPROVE, pageable);
        }
        return convertToDtoPage(petSitters, pageable);
    }

    @Transactional
    public PetSitterResponseDto approvePetSitter(Integer id) {
        PetSitter petSitter = petSitterDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 펫시터입니다"));

        if (petSitter.getStatus() == PetSitter.PetSitterStatus.APPROVE) {
            throw new IllegalArgumentException("이미 승인한 펫시터입니다");
        }

        petSitter.approve();

        PetSitter savedPetSitter = petSitterDao.save(petSitter);

        PetSitterResponseDto dto = PetSitterResponseDto.fromEntity(savedPetSitter);

        String savedUrl = storageService.generatePresignedUrl(savedPetSitter.getFile().getPath());
        dto.setImagePath(savedUrl);

        return dto;
    }

    @Transactional
    public PetSitterResponseDto pendingPetSitter(Integer id) {
        PetSitter petSitter = petSitterDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 펫시터입니다"));

        petSitter.pending();

        PetSitter savedPetSitter = petSitterDao.save(petSitter);

        PetSitterResponseDto dto = PetSitterResponseDto.fromEntity(savedPetSitter);

        String savedUrl = storageService.generatePresignedUrl(savedPetSitter.getFile().getPath());
        dto.setImagePath(savedUrl);

        return dto;
    }

    @Transactional
    public PetSitterResponseDto deletePetSitter(Integer id) {
        PetSitter petSitter = petSitterDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 펫시터 입니다"));

        petSitter.delete();

        PetSitter savedPetSitter = petSitterDao.save(petSitter);

        PetSitterResponseDto dto = PetSitterResponseDto.fromEntity(savedPetSitter);
        String savedUrl = storageService.generatePresignedUrl(savedPetSitter.getFile().getPath());
        dto.setImagePath(savedUrl);

        return dto;
    }

    // 페이지 객체를 DTO로 변환하는 공통 메서드
    private Page<PetSitterResponseDto> convertToDtoPage(Page<PetSitter> petSitters, Pageable pageable) {
        List<PetSitterResponseDto> petSitterDtos = petSitters.getContent().stream()
                .map(petSitter -> {
                    PetSitterResponseDto dto = PetSitterResponseDto.fromEntity(petSitter);
                    String fileUrl = storageService.generatePresignedUrl(petSitter.getFile().getPath());
                    dto.setImagePath(fileUrl);
                    return dto;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(petSitterDtos, pageable, petSitters.getTotalElements());
    }
}
