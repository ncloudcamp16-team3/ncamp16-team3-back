package tf.tailfriend.petsitter.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import tf.tailfriend.global.service.StorageService;
import tf.tailfriend.global.service.StorageServiceException;
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


import tf.tailfriend.user.repository.UserDao;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
@RequiredArgsConstructor
@Slf4j
public class PetSitterService {

    private static final Logger logger = LoggerFactory.getLogger(PetSitterService.class);

    private final PetSitterDao petSitterDao;
    private final StorageService storageService;
    private final UserDao userDao;
    private final PetTypeDao petTypeDao;
    private final FileService fileService;

    @PersistenceContext
    private EntityManager entityManager;


    // 사용자 ID로 펫시터 존재 여부
    @Transactional(readOnly = true)
    public boolean existsById(Integer userId) {
        return petSitterDao.existsById(userId);
    }

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

        PetSitterResponseDto dto = PetSitterResponseDto.fromEntity(petSitter);

        String imageUrl = storageService.generatePresignedUrl(petSitter.getFile().getPath());
        dto.setImagePath(imageUrl);

        return dto;
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
     * 펫시터 신청 처리
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public PetSitterResponseDto applyForPetSitter(PetSitterRequestDto requestDto, MultipartFile imageFile) throws IOException {
        logger.info("펫시터 신청 시작: userId={}", requestDto.getUserId());

        try {
            // 1. 사용자 정보 조회
            User user = userDao.findById(requestDto.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            // 2. 기존 펫시터 정보 확인 및 삭제 - 이 부분을 조건부 삭제로 변경
            PetSitter existing = petSitterDao.findById(user.getId()).orElse(null);
            if (existing != null) {
                logger.info("기존 펫시터 정보 상태 변경: userId={}", user.getId());
                existing.delete();
                petSitterDao.save(existing);
                entityManager.flush();
            }

            // 3. 이미지 파일 처리
            File imageFileEntity;
            if (imageFile != null && !imageFile.isEmpty()) {
                imageFileEntity = fileService.save(imageFile.getOriginalFilename(), "petsitter", File.FileType.PHOTO);
                try (InputStream is = imageFile.getInputStream()) {
                    storageService.upload(imageFileEntity.getPath(), is);
                } catch (StorageServiceException e) {
                    throw new RuntimeException("파일 업로드 실패: " + e.getMessage());
                }
            } else {
                imageFileEntity = fileService.getDefaultImage();
            }

            // 4. 펫 타입 조회 (선택사항)
            PetType petType = null;
            if (requestDto.getPetTypeId() != null) {
                petType = petTypeDao.findById(requestDto.getPetTypeId()).orElse(null);
            }

            // 5. 펫시터 엔티티 생성 및 저장
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
                    .status(PetSitter.PetSitterStatus.NONE) // 승인 대기 상태로 설정
                    .build();

            // 저장 전에 영속성 컨텍스트 초기화
            entityManager.clear();

            // JpaRepository의 save 메서드 사용
            PetSitter savedPetSitter = petSitterDao.save(petSitter);
            entityManager.flush();

            // 6. 응답 DTO 생성
            PetSitterResponseDto responseDto = PetSitterResponseDto.fromEntity(savedPetSitter);
            responseDto.setImagePath(storageService.generatePresignedUrl(imageFileEntity.getPath()));

            logger.info("펫시터 신청 완료: userId={}", user.getId());
            return responseDto;

        } catch (Exception e) {
            logger.error("펫시터 신청 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("펫시터 신청 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 펫시터 상태 조회
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
     * 펫시터 탈퇴 처리
     */
    @Transactional
    public void quitPetSitter(Integer userId) {
        PetSitter petSitter = petSitterDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("펫시터 정보가 없습니다"));

        // 승인된 상태가 아니면 예외 처리
        if (petSitter.getStatus() != PetSitter.PetSitterStatus.APPROVE) {
            throw new IllegalArgumentException("승인된 펫시터만 그만둘 수 있습니다");
        }

        // 펫시터 정보 삭제
        petSitterDao.delete(petSitter);
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