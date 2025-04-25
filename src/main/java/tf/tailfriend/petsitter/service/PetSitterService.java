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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

@Service
@RequiredArgsConstructor
public class PetSitterService {

    private static final Logger logger = LoggerFactory.getLogger(PetSitterService.class);

    private final PetSitterDao petSitterDao;
    private final StorageService storageService;
    private final UserDao userDao;
    private final PetTypeDao petTypeDao;
    private final FileService fileService;

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
     * 동시성 제어를 위한 간단한 메커니즘 적용 (DB락 대신 애플리케이션 레벨 동기화)
     */
    @Transactional
    public PetSitterResponseDto applyForPetSitter(PetSitterRequestDto requestDto, MultipartFile imageFile) throws IOException {
        try {
//            petSitterDao.findById(requestDto.getUserId()).ifPresent(petSitter -> {
//                petSitterDao.delete(petSitter);
//                petSitterDao.flush();
//            });
            logger.info("펫시터 신청 시작: userId={}", requestDto.getUserId());

            // 사용자 정보 조회
            User user = userDao.findById(requestDto.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다: " + requestDto.getUserId()));

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

            // 펫 타입 조회 (선택사항)
            PetType petType = null;
            if (requestDto.getPetTypeId() != null) {
                petType = petTypeDao.findById(requestDto.getPetTypeId())
                        .orElse(null);
            }

            // 기존 펫시터 정보 확인
            Optional<PetSitter> existingSitterOpt = petSitterDao.findById(user.getId());
            PetSitter petSitter;

            if (existingSitterOpt.isPresent()) {
                // 기존 정보가 있으면 업데이트
                logger.info("기존 펫시터 정보 업데이트: userId={}", user.getId());
                PetSitter existingSitter = existingSitterOpt.get();

                // updateInformation 메소드 사용하여 정보 업데이트
                existingSitter.updateInformation(
                        requestDto.getAge(),
                        requestDto.getHouseType(),
                        requestDto.getComment(),
                        requestDto.getGrown(),
                        requestDto.getPetCount(),
                        requestDto.getSitterExp(),
                        imageFileEntity,
                        petType
                );

                // 상태 변경
                existingSitter.waitForApproval();

                petSitter = petSitterDao.save(existingSitter);
            } else {
                // 새로 생성
                logger.info("새로운 펫시터 정보 생성: userId={}", user.getId());
                petSitter = PetSitter.builder()
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
                        .status(PetSitter.PetSitterStatus.NONE) // NONE 상태로 설정
                        .build();

                petSitter = petSitterDao.save(petSitter);
            }

            logger.info("펫시터 정보 저장 완료: userId={}, status={}", petSitter.getId(), petSitter.getStatus());

            // 응답 DTO 생성
            PetSitterResponseDto responseDto = PetSitterResponseDto.fromEntity(petSitter);

            // 이미지 URL 설정
            String imageUrl = storageService.generatePresignedUrl(imageFileEntity.getPath());
            responseDto.setImagePath(imageUrl);

            return responseDto;

        } catch (DataIntegrityViolationException e) {
            logger.error("펫시터 신청 중 데이터 무결성 오류 발생: {}", e.getMessage());
            throw new IllegalStateException("데이터 무결성 오류가 발생했습니다. 입력한 정보를 확인해주세요.", e);
        } catch (Exception e) {
            logger.error("펫시터 신청 중 예외 발생: {}", e.getMessage(), e);
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

    @Transactional
    public void cleanupExistingPetSitter(Integer userId) {
        petSitterDao.findById(userId).ifPresent(existing -> {
            logger.info("기존 펫시터 정보 삭제: userId={}", userId);
            petSitterDao.delete(existing);
            petSitterDao.flush(); // 즉시 DB에 반영
        });
    }
}