package tf.tailfriend.petsitter.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tf.tailfriend.file.entity.File;
import tf.tailfriend.file.service.FileService;
import tf.tailfriend.global.service.StorageService;
import tf.tailfriend.global.service.StorageServiceException;
import tf.tailfriend.pet.entity.PetType;
import tf.tailfriend.pet.repository.PetTypeDao;
import tf.tailfriend.petsitter.dto.PetSitterRequestDto;
import tf.tailfriend.petsitter.dto.PetSitterResponseDto;
import tf.tailfriend.petsitter.entity.PetSitter;
import tf.tailfriend.petsitter.repository.PetSitterDao;
import tf.tailfriend.user.entity.User;
import tf.tailfriend.user.repository.UserDao;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Autowired
    private EntityManager entityManager;

    /**
     * 사용자의 현재 펫시터 상태를 확인하는 메소드
     *
     * @param userId 사용자 ID
     * @return 펫시터 상태 (PENDING, APPROVE, DELETE, NONE) 또는 null(정보 없음)
     */
    @Transactional(readOnly = true)
    public PetSitter.PetSitterStatus checkCurrentStatus(Integer userId) {
        Optional<PetSitter> petSitter = petSitterDao.findById(userId);
        return petSitter.map(PetSitter::getStatus).orElse(null);
    }

    /**
     * 사용자 ID로 펫시터 존재 여부 확인
     *
     * @param userId 사용자 ID
     * @return 존재 여부
     */
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

        // 이미지 URL 설정
        if (petSitter.getFile() != null) {
            String imageUrl = storageService.generatePresignedUrl(petSitter.getFile().getPath());
            dto.setImagePath(imageUrl);
        }

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

        // 이미지 URL 설정
        if (savedPetSitter.getFile() != null) {
            String savedUrl = storageService.generatePresignedUrl(savedPetSitter.getFile().getPath());
            dto.setImagePath(savedUrl);
        }

        return dto;
    }

    @Transactional
    public PetSitterResponseDto pendingPetSitter(Integer id) {
        PetSitter petSitter = petSitterDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 펫시터입니다"));

        petSitter.pending();

        PetSitter savedPetSitter = petSitterDao.save(petSitter);

        PetSitterResponseDto dto = PetSitterResponseDto.fromEntity(savedPetSitter);

        // 이미지 URL 설정
        if (savedPetSitter.getFile() != null) {
            String savedUrl = storageService.generatePresignedUrl(savedPetSitter.getFile().getPath());
            dto.setImagePath(savedUrl);
        }

        return dto;
    }

    @Transactional
    public PetSitterResponseDto deletePetSitter(Integer id) {
        PetSitter petSitter = petSitterDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 펫시터 입니다"));

        petSitter.delete();

        PetSitter savedPetSitter = petSitterDao.save(petSitter);

        PetSitterResponseDto dto = PetSitterResponseDto.fromEntity(savedPetSitter);

        // 이미지 URL 설정
        if (savedPetSitter.getFile() != null) {
            String savedUrl = storageService.generatePresignedUrl(savedPetSitter.getFile().getPath());
            dto.setImagePath(savedUrl);
        }

        return dto;
    }

    /**
     * 사용자의 펫시터 신청을 처리하는 메소드
     * Native Query를 활용하여 동시성 충돌 방지
     */
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public PetSitterResponseDto applyForPetSitter(PetSitterRequestDto requestDto, MultipartFile imageFile) throws IOException {
        logger.info("펫시터 신청 시작: userId={}", requestDto.getUserId());

        try {
            // 사용자 정보 조회
            User user = userDao.findById(requestDto.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다: " + requestDto.getUserId()));

            // 이미지 파일 처리
            File imageFileEntity;
            if (imageFile != null && !imageFile.isEmpty()) {
                imageFileEntity = fileService.save(imageFile.getOriginalFilename(), "petsitter", File.FileType.PHOTO);
                try (InputStream is = imageFile.getInputStream()) {
                    try {
                        storageService.upload(imageFileEntity.getPath(), is);
                    } catch (StorageServiceException e) {
                        logger.error("이미지 업로드 실패", e);
                        throw new RuntimeException("파일 업로드 실패: " + e.getMessage());
                    }
                }
            } else {
                imageFileEntity = fileService.getDefaultImage();
            }

            // 펫 타입 조회
            PetType petType = null;
            if (requestDto.getPetTypeId() != null) {
                petType = petTypeDao.findById(requestDto.getPetTypeId()).orElse(null);
            }

            // 트랜잭션 밖에서 사용자 ID로 펫시터 존재 여부 확인
            boolean exists = petSitterDao.existsById(requestDto.getUserId());

            Integer petTypeId = petType != null ? petType.getId() : null;
            Integer fileId = imageFileEntity.getId();

            // 네이티브 쿼리 실행을 위한 EntityManager 사용
            if (exists) {
                // 기존 데이터 업데이트 쿼리
                String updateQuery =
                        "UPDATE pet_sitters SET " +
                                "age = ?1, " +
                                "house_type = ?2, " +
                                "comment = ?3, " +
                                "grown = ?4, " +
                                "pet_count = ?5, " +
                                "sitter_exp = ?6, " +
                                "file_id = ?7, " +
                                "pet_type_id = ?8, " +
                                "status = 'NONE', " +
                                "apply_at = NULL " +
                                "WHERE id = ?9";

                // 쿼리 파라미터 설정
                Query query = entityManager.createNativeQuery(updateQuery);
                query.setParameter(1, requestDto.getAge());
                query.setParameter(2, requestDto.getHouseType());
                query.setParameter(3, requestDto.getComment());
                query.setParameter(4, requestDto.getGrown());
                query.setParameter(5, requestDto.getPetCount().name());
                query.setParameter(6, requestDto.getSitterExp());
                query.setParameter(7, fileId);
                query.setParameter(8, petTypeId);
                query.setParameter(9, requestDto.getUserId());

                // 쿼리 실행
                int updated = query.executeUpdate();
                logger.info("펫시터 정보 업데이트 완료: userId={}, rows={}", requestDto.getUserId(), updated);
            } else {
                // 새 데이터 삽입 쿼리
                String insertQuery =
                        "INSERT INTO pet_sitters (id, age, house_type, comment, grown, pet_count, sitter_exp, file_id, pet_type_id, status, created_at) " +
                                "VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, 'NONE', NOW())";

                // 쿼리 파라미터 설정
                Query query = entityManager.createNativeQuery(insertQuery);
                query.setParameter(1, requestDto.getUserId());
                query.setParameter(2, requestDto.getAge());
                query.setParameter(3, requestDto.getHouseType());
                query.setParameter(4, requestDto.getComment());
                query.setParameter(5, requestDto.getGrown());
                query.setParameter(6, requestDto.getPetCount().name());
                query.setParameter(7, requestDto.getSitterExp());
                query.setParameter(8, fileId);
                query.setParameter(9, petTypeId);

                // 쿼리 실행
                int inserted = query.executeUpdate();
                logger.info("펫시터 정보 삽입 완료: userId={}, rows={}", requestDto.getUserId(), inserted);
            }

            // DB에서 다시 데이터 조회하여 응답 생성
            entityManager.clear(); // 영속성 컨텍스트 초기화

            PetSitter updatedPetSitter = petSitterDao.findById(requestDto.getUserId())
                    .orElseThrow(() -> new IllegalStateException("저장된 펫시터 정보를 조회할 수 없습니다"));

            PetSitterResponseDto responseDto = PetSitterResponseDto.fromEntity(updatedPetSitter);

            // 이미지 URL 설정
            String imageUrl = storageService.generatePresignedUrl(imageFileEntity.getPath());
            responseDto.setImagePath(imageUrl);

            return responseDto;
        } catch (Exception e) {
            logger.error("펫시터 신청 처리 중 오류 발생", e);
            throw e;
        }
    }

    /**
     * 사용자의 펫시터 신청 상태를 조회하는 메소드
     */
    @Transactional(readOnly = true)
    public PetSitterResponseDto getPetSitterStatus(Integer userId) {
        PetSitter petSitter = petSitterDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("펫시터 정보가 없습니다"));

        PetSitterResponseDto responseDto = PetSitterResponseDto.fromEntity(petSitter);

        // 이미지 URL 설정
        if (petSitter.getFile() != null) {
            String imageUrl = storageService.generatePresignedUrl(petSitter.getFile().getPath());
            responseDto.setImagePath(imageUrl);
        }

        return responseDto;
    }

    /**
     * 사용자가 펫시터를 그만두는 메소드
     * 격리 수준 READ_UNCOMMITTED로 변경하여 동시성 향상
     */
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public void quitPetSitter(Integer userId) {
        logger.info("펫시터 그만두기 요청: userId={}", userId);

        PetSitter petSitter = petSitterDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("펫시터 정보가 없습니다"));

        // 승인된 상태가 아니면 예외 처리
        if (petSitter.getStatus() != PetSitter.PetSitterStatus.APPROVE) {
            logger.warn("승인되지 않은 펫시터의 그만두기 요청: status={}", petSitter.getStatus());
            throw new IllegalArgumentException("승인된 펫시터만 그만둘 수 있습니다");
        }

        // Native 쿼리 사용하여 상태 변경
        String updateQuery = "UPDATE pet_sitters SET status = 'DELETE' WHERE id = ?1";
        Query query = entityManager.createNativeQuery(updateQuery);
        query.setParameter(1, userId);

        int updated = query.executeUpdate();
        logger.info("펫시터 상태 변경 완료 (DELETE): userId={}, rows={}", userId, updated);
    }

    // 페이지 객체를 DTO로 변환하는 공통 메서드
    private Page<PetSitterResponseDto> convertToDtoPage(Page<PetSitter> petSitters, Pageable pageable) {
        List<PetSitterResponseDto> petSitterDtos = petSitters.getContent().stream()
                .map(petSitter -> {
                    PetSitterResponseDto dto = PetSitterResponseDto.fromEntity(petSitter);

                    // 이미지 URL 설정
                    if (petSitter.getFile() != null) {
                        String fileUrl = storageService.generatePresignedUrl(petSitter.getFile().getPath());
                        dto.setImagePath(fileUrl);
                    }

                    return dto;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(petSitterDtos, pageable, petSitters.getTotalElements());
    }
}