package tf.tailfriend.petsitter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tf.tailfriend.global.service.StorageService;
import tf.tailfriend.pet.entity.PetType;
import tf.tailfriend.petsitter.dto.PetSitterRequestDto;
import tf.tailfriend.petsitter.dto.PetSitterResponseDto;
import tf.tailfriend.petsitter.entity.PetSitter;
import tf.tailfriend.petsitter.repository.PetSitterDao;
import tf.tailfriend.user.entity.User;
import org.springframework.web.multipart.MultipartFile;
import tf.tailfriend.file.entity.File;
import tf.tailfriend.file.service.FileService;
import tf.tailfriend.pet.repository.PetTypeDao;
import tf.tailfriend.global.service.StorageServiceException;
import tf.tailfriend.user.repository.UserDao;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PetSitterService {

    private final PetSitterDao petSitterDao;
    private final StorageService storageService;
    private final UserDao userDao;
    private final PetTypeDao petTypeDao;
    private final FileService fileService;


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


    /**
     * 사용자의 펫시터 신청을 처리하는 메소드
     */
    @Transactional
    public PetSitterResponseDto applyForPetSitter(PetSitterRequestDto requestDto, MultipartFile imageFile) throws IOException {
        User user = userDao.findById(requestDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다"));

        // 이미 신청한 경우 체크
        Optional<PetSitter> existingSitter = petSitterDao.findById(user.getId());
        if (existingSitter.isPresent()) {
            PetSitter existing = existingSitter.get();
            if (existing.getStatus() == PetSitter.PetSitterStatus.APPROVE) {
                throw new IllegalArgumentException("이미 승인된 펫시터입니다");
            } else if (existing.getStatus() == PetSitter.PetSitterStatus.PENDING) {
                throw new IllegalArgumentException("승인 대기 중인 신청이 있습니다");
            }
        }

        // 펫 타입 조회 (선택사항)
        PetType petType = null;
        if (requestDto.getPetTypeId() != null) {
            petType = petTypeDao.findById(requestDto.getPetTypeId())
                    .orElse(null);
        }

        // 이미지 파일 처리
        File imageFileEntity;
        if (imageFile != null && !imageFile.isEmpty()) {
            imageFileEntity = fileService.save(imageFile.getOriginalFilename(), "petsitter", File.FileType.PHOTO);

            // 물리적 파일 저장
            try (InputStream is = imageFile.getInputStream()) {
                storageService.upload(imageFileEntity.getPath(), is);
            } catch (StorageServiceException e) {
                throw new RuntimeException("파일 업로드 실패: " + e.getMessage());
            }
        } else {
            // 기본 이미지 사용
            imageFileEntity = fileService.getDefaultImage();
        }

        // PetSitter 엔티티 생성
        PetSitter petSitter = PetSitter.builder()
                .id(user.getId())
                .user(user)
                .petType(petType)
                .age(requestDto.getAge())
                .houseType(requestDto.getHouseType())
                .comment(requestDto.getComment())
                .grown(requestDto.getGrown())
                .petCount(requestDto.getPetCount())
                .sitterExp(requestDto.getSitterExp())
                .file(imageFileEntity)
                .status(PetSitter.PetSitterStatus.PENDING) // 항상 PENDING으로 시작
                .build();

        PetSitter savedPetSitter = petSitterDao.save(petSitter);

        // 응답 DTO 생성
        PetSitterResponseDto responseDto = PetSitterResponseDto.fromEntity(savedPetSitter);

        // 이미지 URL 설정
        String imageUrl = storageService.generatePresignedUrl(imageFileEntity.getPath());
        responseDto.setImagePath(imageUrl);

        return responseDto;
    }

    /**
     * 사용자의 펫시터 신청 상태를 조회하는 메소드
     */
    @Transactional(readOnly = true)
    public PetSitterResponseDto getPetSitterStatus(Integer userId) {
        PetSitter petSitter = petSitterDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("펫시터 정보가 없습니다"));

        PetSitterResponseDto responseDto = PetSitterResponseDto.fromEntity(petSitter);
        String imageUrl = storageService.generatePresignedUrl(petSitter.getFile().getPath());
        responseDto.setImagePath(imageUrl);

        return responseDto;
    }

    /**
     * 사용자가 펫시터를 그만두는 메소드
     */
    @Transactional
    public void quitPetSitter(Integer userId) {
        PetSitter petSitter = petSitterDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("펫시터 정보가 없습니다"));

        // 승인된 상태가 아니면 예외 처리
        if (petSitter.getStatus() != PetSitter.PetSitterStatus.APPROVE) {
            throw new IllegalArgumentException("승인된 펫시터만 그만둘 수 있습니다");
        }

        // 상태를 DELETE로 변경
        petSitter.delete();
        petSitterDao.save(petSitter);
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
