package tf.tailfriend.petsitter.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    //사용자 ID로 펫시터 존재 여부 확인
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

    // 사용자의 펫시터 신청을 처리하는 메소드
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

            //사용자 ID로 펫시터 존재 여부 확인
            boolean exists = petSitterDao.existsById(requestDto.getUserId());

            Integer petTypeId = petType != null ? petType.getId() : null;
            Integer fileId = imageFileEntity.getId();

            //EntityManager 사용
            if (exists) {
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
            entityManager.clear();

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

    // 사용자의 펫시터 신청 상태를 조회하는 메소드
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

    //사용자가 펫시터를 그만두는 메소드
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

    /**
     * 승인된 펫시터 중 검색 조건에 맞는 펫시터 목록을 조회하는 메소드
     *
     * @param age          연령대 (20대, 30대, 40대, 50대이상)
     * @param petOwnership 반려동물 소유 여부
     * @param sitterExp    펫시터 경험 여부
     * @param houseType    주거 형태
     * @param pageable     페이징 정보
     * @return 조건에 맞는 펫시터 목록 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<PetSitterResponseDto> findApprovedPetSittersWithCriteria(
            String age, Boolean petOwnership, Boolean sitterExp, String houseType, Pageable pageable) {

        logger.info("승인된 펫시터 검색 시작: age={}, petOwnership={}, sitterExp={}, houseType={}",
                age, petOwnership, sitterExp, houseType);

        //기본 쿼리
        String baseQuery = "SELECT ps FROM PetSitter ps WHERE ps.status = :status";
        String countQuery = "SELECT COUNT(ps) FROM PetSitter ps WHERE ps.status = :status";

        // 조건에 따라 동적으로 쿼리 구성
        StringBuilder queryBuilder = new StringBuilder(baseQuery);
        StringBuilder countQueryBuilder = new StringBuilder(countQuery);

        // 파라미터 맵
        Map<String, Object> params = new HashMap<>();
        params.put("status", PetSitter.PetSitterStatus.APPROVE);

        // 연령대 조건
        if (age != null && !age.isEmpty()) {
            queryBuilder.append(" AND ps.age = :age");
            countQueryBuilder.append(" AND ps.age = :age");
            params.put("age", age);
        }

        // 반려동물 소유 여부 조건
        if (petOwnership != null) {
            queryBuilder.append(" AND ps.grown = :grown");
            countQueryBuilder.append(" AND ps.grown = :grown");
            params.put("grown", petOwnership);
        }

        // 펫시터 경험 여부 조건
        if (sitterExp != null) {
            queryBuilder.append(" AND ps.sitterExp = :sitterExp");
            countQueryBuilder.append(" AND ps.sitterExp = :sitterExp");
            params.put("sitterExp", sitterExp);
        }

        // 주거 형태 조건
        if (houseType != null && !houseType.isEmpty()) {
            queryBuilder.append(" AND ps.houseType = :houseType");
            countQueryBuilder.append(" AND ps.houseType = :houseType");
            params.put("houseType", houseType);
        }

        // 쿼리 실행
        TypedQuery<PetSitter> query = entityManager.createQuery(queryBuilder.toString(), PetSitter.class);
        TypedQuery<Long> countQueryResult = entityManager.createQuery(countQueryBuilder.toString(), Long.class);

        // 파라미터 설정
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
            countQueryResult.setParameter(entry.getKey(), entry.getValue());
        }
        // 페이징 적용
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        // 결과 조회
        List<PetSitter> petSitters = query.getResultList();
        Long total = countQueryResult.getSingleResult();

        // DTO 변환
        List<PetSitterResponseDto> dtoList = petSitters.stream()
                .map(petSitter -> {
                    PetSitterResponseDto dto = PetSitterResponseDto.fromEntity(petSitter);

                    // 이미지 URL 설정
                    if (petSitter.getFile() != null) {
                        String imageUrl = storageService.generatePresignedUrl(petSitter.getFile().getPath());
                        dto.setImagePath(imageUrl);
                    }

                    return dto;
                })
                .collect(Collectors.toList());

        logger.info("승인된 펫시터 검색 완료: 결과 수={}", dtoList.size());

        return new PageImpl<>(dtoList, pageable, total);
    }
}