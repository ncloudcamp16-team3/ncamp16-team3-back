package tf.tailfriend.facility.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tf.tailfriend.facility.dto.*;
import tf.tailfriend.facility.entity.*;
import tf.tailfriend.facility.entity.dto.forReserve.FacilityCardResponseDto;
import tf.tailfriend.facility.entity.dto.forReserve.FacilityWithDistanceProjection;
import tf.tailfriend.facility.entity.dto.forReserve.ThumbnailForCardDto;
import tf.tailfriend.facility.repository.*;
import tf.tailfriend.file.entity.File;
import tf.tailfriend.file.repository.FileDao;
import tf.tailfriend.file.service.FileService;
import tf.tailfriend.global.exception.CustomException;
import tf.tailfriend.global.service.StorageService;
import tf.tailfriend.global.service.StorageServiceException;
import tf.tailfriend.reserve.dto.RequestForFacility.FacilityList;
import tf.tailfriend.reserve.dto.RequestForFacility.ReviewInsertRequestDto;
import tf.tailfriend.reserve.entity.Reserve;
import tf.tailfriend.reserve.repository.ReserveDao;
import tf.tailfriend.user.entity.User;
import tf.tailfriend.user.repository.UserDao;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class FacilityService {

    private final FacilityDao facilityDao;
    private final FacilityTypeDao facilityTypeDao;
    private final FacilityTimetableDao facilityTimetableDao;
    private final FacilityPhotoDao facilityPhotoDao;
    private final ReviewDao reviewDao;
    private final UserDao userDao;
    private final StorageService storageService;
    private final FileService fileService;
    private final FileDao fileDao;
    private final ReviewPhotoDao reviewPhotoDao;
    private final ReserveDao reserveDao;

    @Transactional(readOnly = true)
    public Page<FacilityResponseDto> findAll(Pageable pageable) {
        Page<Facility> facilities = facilityDao.findAll(pageable);
        return convertToDtoPage(facilities, pageable);
    }

    @Transactional(readOnly = true)
    public Page<FacilityResponseDto> findByFacilityType(Integer facilityTypeId, Pageable pageable) {
        FacilityType facilityType = facilityTypeDao.findById(facilityTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid facility type"));

        Page<Facility> facilities = facilityDao.findByFacilityType(facilityType, pageable);
        return convertToDtoPage(facilities, pageable);
    }

    @Transactional(readOnly = true)
    public Page<FacilityResponseDto> searchFacilities(
            Integer facilityTypeId, String searchTerm, String searchField, Pageable pageable) {
        log.info("facilityTypeId: {}", facilityTypeId);
        Page<Facility> facilities;

        // 검색 필드에 따른 다양한 검색 쿼리 실행
        if ("name".equals(searchField)) {
            // 이름으로 검색
            if (facilityTypeId != null) {
                FacilityType facilityType = facilityTypeDao.findById(facilityTypeId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid facility type"));
                facilities = facilityDao.findByFacilityTypeAndNameContaining(facilityType, searchTerm, pageable);
            } else {
                facilities = facilityDao.findByNameContaining(searchTerm, pageable);
            }
        } else if ("address".equals(searchField)) {
            // 주소로 검색
            if (facilityTypeId != null) {
                FacilityType facilityType = facilityTypeDao.findById(facilityTypeId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid facility type"));
                facilities = facilityDao.findByFacilityTypeAndAddressContaining(facilityType, searchTerm, pageable);
            } else {
                facilities = facilityDao.findByAddressContaining(searchTerm, pageable);
            }
        } else if ("tel".equals(searchField)) {
            // 전화번호로 검색
            if (facilityTypeId != null) {
                FacilityType facilityType = facilityTypeDao.findById(facilityTypeId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid facility type"));
                facilities = facilityDao.findByFacilityTypeAndTelContaining(facilityType, searchTerm, pageable);
            } else {
                facilities = facilityDao.findByTelContaining(searchTerm, pageable);
            }
        } else if ("detail".equals(searchField)) {
            // 상세내용으로 검색
            if (facilityTypeId != null) {
                FacilityType facilityType = facilityTypeDao.findById(facilityTypeId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid facility type"));
                facilities = facilityDao.findByFacilityTypeAndCommentContaining(facilityType, searchTerm, pageable);
            } else {
                facilities = facilityDao.findByCommentContaining(searchTerm, pageable);
            }
        } else {
            // 기본: 전체 목록
            if (facilityTypeId != null) {
                FacilityType facilityType = facilityTypeDao.findById(facilityTypeId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid facility type"));
                facilities = facilityDao.findByFacilityType(facilityType, pageable);
            } else {
                facilities = facilityDao.findAll(pageable);
            }
        }

        return facilities.map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public FacilityResponseDto getFacilityById(Integer id) {
        Facility facility = facilityDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("시설을 찾을 수 없습니다: " + id));

        return convertToDto(facility);
    }

    @Transactional
    public FacilityAddResponseDto saveFacility(FacilityRequestDto requestDto, List<MultipartFile> images) {
        FacilityType facilityType = facilityTypeDao.findById(requestDto.getFacilityTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid facility type"));

        Facility facility = Facility.builder()
                .name(requestDto.getName())
                .facilityType(facilityType)
                .tel(requestDto.getTel())
                .comment(requestDto.getComment())
                .address(requestDto.getAddress())
                .detailAddress(requestDto.getDetailAddress())
                .latitude(requestDto.getLatitude())
                .longitude(requestDto.getLongitude())
                .reviewCount(0)
                .totalStarPoint(0)
                .createdAt(LocalDateTime.now())
                .build();

        Facility savedFacility = facilityDao.save(facility);
        log.info("saved facility: {}", savedFacility);

        saveWeeklyTimetables(
                savedFacility,
                requestDto.getOpenTimes(),
                requestDto.getCloseTimes(),
                requestDto.getOpenDays()
        );

        List<File> savedFiles = saveImages(savedFacility, images);

        return convertToResponseDto(savedFacility, savedFiles);
    }

    @Transactional
    public FacilityAddResponseDto updateFacility(Integer id, FacilityRequestDto requestDto, List<MultipartFile> newImages, List<String> imageIdsToKeep) {
        log.info("시설 ID: {}의 이미지 업데이트", id);
        log.info("유지할 이미지 ID: {}", imageIdsToKeep);
        log.info("새 이미지 개수: {}", newImages != null ? newImages.size() : 0);

        Facility facility = facilityDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("등록된 업체가 없습니다"));

        FacilityType facilityType = facilityTypeDao.findById(requestDto.getFacilityTypeId())
                .orElseThrow(() -> new IllegalArgumentException("등록된 시설 유형이 없습니다"));

        facility.updateInformation(
                facilityType,
                requestDto.getName(),
                requestDto.getTel(),
                requestDto.getAddress(),
                requestDto.getDetailAddress(),
                requestDto.getComment(),
                requestDto.getLatitude(),
                requestDto.getLongitude()
        );

        updateWeeklyTimetables(
                facility,
                requestDto.getOpenTimes(),
                requestDto.getCloseTimes(),
                requestDto.getOpenDays()
        );

        List<File> updatedFiles;

        if (newImages == null) {
            newImages = Collections.emptyList();
        }

        if (imageIdsToKeep == null) {
            imageIdsToKeep = Collections.emptyList();
        }

        // 1. newImages가 비어있고 imageIdsToKeep도 비어있으면 → 모든 이미지 삭제
        // 2. imageIdsToKeep가 있으면 → 지정된 이미지만 유지하고 나머지 삭제
        // 3. newImages가 있으면 → 새 이미지 추가
        if (newImages.isEmpty() && imageIdsToKeep.isEmpty()) {
            log.info("모든 이미지 삭제 요청 (newImages 없음, imageIdsToKeep 없음)");
            // 모든 이미지 삭제
            updatedFiles = updateImages(facility, Collections.emptyList(), Collections.emptyList());
        } else {
            // 특정 이미지 유지 및/또는 새 이미지 추가
            log.info("이미지 선택적 업데이트 (유지할 이미지: {}, 새 이미지: {})",
                    imageIdsToKeep.size(), newImages.size());
            updatedFiles = updateImages(facility, newImages, imageIdsToKeep);
        }

        log.info("이미지 업데이트 완료 - 최종 이미지 개수: {}", updatedFiles.size());
        return convertToResponseDto(facility, updatedFiles);
    }

    @Transactional
    public void deleteFacilityById(Integer facilityId) {
        Facility facility = facilityDao.findById(facilityId)
                .orElseThrow(() -> new IllegalArgumentException("등록된 업체가 없습니다"));

        facilityDao.delete(facility);
    }

    @Transactional(readOnly = true)
    public FacilityDetailDto getFacilityDetailWithReviews(Integer facilityId, Integer userId) {
        Facility facility = facilityDao.findById(facilityId)
                .orElseThrow(() -> new IllegalArgumentException("시설을 찾을 수 없습니다: " + facilityId));

        FacilityResponseDto facilityDto = convertToDto(facility);
        Integer totalStarPoint = facility.getTotalStarPoint();
        Integer reviewCount = facility.getReviewCount();
        if (totalStarPoint == 0 || reviewCount == 0) {
            facilityDto.setStarPoint(0.0);
        } else {
            double avg = (double) totalStarPoint / reviewCount;
            facilityDto.setStarPoint(avg);
        }

        log.info("facilityDto: {}", facilityDto);

        List<ReviewResponseDto> reviewDtos = getReviewDtos(facilityId);

        List<Object[]> reviewRatio = reviewDao.countReviewsByStarPoint(facilityId);

        return new FacilityDetailDto(facilityDto, reviewDtos, reviewRatio, userId);
    }

    private List<ReviewResponseDto> getReviewDtos(Integer facilityId) {
        // 1. 시설 ID로 리뷰 목록 조회
        List<Review> reviews = reviewDao.findByFacilityIdOrderByCreatedAtDesc(facilityId);

        // 2. 리뷰 DTO 목록 생성
        List<ReviewResponseDto> reviewDtos = new ArrayList<>();

        // 3. 각 리뷰를 DTO로 변환하여 목록에 추가
        for (Review review : reviews) {
            User user = review.getUser();

            // 리뷰 기본 정보 생성
            ReviewResponseDto reviewDto = ReviewResponseDto.builder()
                    .id(review.getId())
                    .userId(user.getId())
                    .userName(user.getNickname())
                    .comment(review.getComment())
                    .starPoint(review.getStarPoint())
                    .createdAt(review.getCreatedAt())
                    .build();

            // 사용자 프로필 이미지 URL 설정
            if (user.getFile() != null && user.getFile().getId() != null) {
                reviewDto.setUserProfileImage(
                        storageService.generatePresignedUrl(user.getFile().getPath())
                );
            }

            // 리뷰 이미지 URL 목록 설정
            List<ReviewPhoto> reviewPhotos = reviewPhotoDao.findByReviewId(review.getId());
            if (reviewPhotos != null && !reviewPhotos.isEmpty()) {
                List<String> reviewImageUrls = reviewPhotos.stream()
                        .map(reviewPhoto -> storageService.generatePresignedUrl(reviewPhoto.getFile().getPath()))
                        .collect(Collectors.toList());
                reviewDto.setReviewImages(reviewImageUrls);
            } else {
                reviewDto.setReviewImages(Collections.emptyList());
            }

            reviewDtos.add(reviewDto);
        }

        return reviewDtos;
    }

    private void saveWeeklyTimetables(Facility facility, Map<String, String> openTimes, Map<String, String> closeTimes, Map<String, Boolean> openDays) {
        List<FacilityTimetable> timetables = new ArrayList<>();

        for (FacilityTimetable.Day day : FacilityTimetable.Day.values()) {
            String dayName = day.name();

            boolean isOpen = openDays == null || Boolean.TRUE.equals(openDays.get(dayName));

            FacilityTimetable timetable;

            if (isOpen) {
                String openTimeStr = openTimes.get(dayName);
                String closeTimeStr = closeTimes.get(dayName);

                try {
                    Time openTime = Time.valueOf(openTimeStr + ":00");
                    Time closeTime = Time.valueOf(closeTimeStr + ":00");

                    timetable = FacilityTimetable.builder()
                            .facility(facility)
                            .day(day)
                            .openTime(openTime)
                            .closeTime(closeTime)
                            .build();
                } catch (IllegalArgumentException e) {
                    log.error("시설 ID {}의 요일 {} 시간 반환 오류: {}", facility.getId(), dayName, e.getMessage());
                    continue;
                }
            } else {
                log.info("시설 ID {}의 요일 {} 휴무일 처리: null 시간 설정", facility.getId(), dayName);
                timetable = FacilityTimetable.builder()
                        .facility(facility)
                        .day(day)
                        .openTime(null)
                        .closeTime(null)
                        .build();
            }
            timetables.add(timetable);
        }

        if (!timetables.isEmpty()) {
            facilityTimetableDao.saveAll(timetables);
            log.info("시설 ID {}에 요일별 영업시간 {}건 저장완료", facility.getId(), timetables.size());
        }
    }

    private void updateWeeklyTimetables(Facility facility, Map<String, String> openTimes, Map<String, String> closeTimes, Map<String, Boolean> openDays) {
        List<FacilityTimetable> timetables = new ArrayList<>();

        for (FacilityTimetable.Day day : FacilityTimetable.Day.values()) {
            String dayName = day.name();

            boolean isOpen = openDays == null || Boolean.TRUE.equals(openDays.get(dayName));

            FacilityTimetable timetable;

            if (isOpen) {
                String openTimeStr = openTimes.get(dayName);
                String closeTimeStr = closeTimes.get(dayName);

                try {
                    Time openTime = Time.valueOf(openTimeStr + ":00");
                    Time closeTime = Time.valueOf(closeTimeStr + ":00");

                    timetable = facilityTimetableDao.findByFacilityAndDay(facility, day)
                            .orElse(FacilityTimetable.builder()
                                    .facility(facility)
                                    .day(day)
                                    .build());

                    timetable = timetable.toBuilder()
                            .openTime(openTime)
                            .closeTime(closeTime)
                            .build();
                } catch (IllegalArgumentException e) {
                    log.error("시설 ID {}의 요일 {} 시간 반환 오류: {}", facility.getId(), dayName, e.getMessage());
                    continue;
                }
            } else {
                log.info("시설 ID {}의 요일 {} 휴무일 처리: null 시간 설정", facility.getId(), dayName);
                timetable = facilityTimetableDao.findByFacilityAndDay(facility, day)
                        .orElse(FacilityTimetable.builder()
                                .facility(facility)
                                .day(day)
                                .build());

                timetable = timetable.toBuilder()
                        .openTime(null)
                        .closeTime(null)
                        .build();
            }

            timetables.add(timetable);
        }

        if (!timetables.isEmpty()) {
            facilityTimetableDao.saveAll(timetables);
            log.info("시설 ID {}에 요일별 영업시간 {}건 업데이트 완료", facility.getId(), timetables.size());
        }
    }

    private List<File> saveImages(Facility facility, List<MultipartFile> images) {
        List<File> savedFiles = new ArrayList<>();

        if (images == null || images.isEmpty()) {
            log.info("시설 ID {}에 업로드된 이미지 없음", facility.getId());
            return savedFiles;
        }

        for (MultipartFile image : images) {
            if (image.isEmpty()) {
                continue;
            }

            validateImage(image);

            String originalFilename = image.getOriginalFilename();
            File fileEntity = fileService.save(originalFilename, "facility", File.FileType.PHOTO);
            savedFiles.add(fileEntity);

            try (InputStream inputStream = image.getInputStream()) {
                storageService.upload(fileEntity.getPath(), inputStream);
                log.info("파일 업로드 성공: {}", fileEntity.getPath());
            } catch (IOException e) {
                log.error("파일 스트림 처리 중 오류: {}", e.getMessage(), e);
            } catch (StorageServiceException e) {
                log.error("스토리지 업로드 중 오류: {}", e.getMessage(), e);
            }

            FacilityPhoto facilityPhoto = FacilityPhoto.of(fileEntity, facility);
            facilityPhotoDao.save(facilityPhoto);

            log.info("시설 ID {}에 이미지 ID {} 연결 완료", facility.getId(), facilityPhoto.getId());
        }
        log.info("시설 ID {}에 총 {}개 이미지 저장 완료", facility.getId(), savedFiles.size());
        return savedFiles;
    }

    private List<File> updateImages(Facility facility, List<MultipartFile> newImages, List<String> fileNamesToKeep) {
        if (newImages == null) {
            newImages = Collections.emptyList();
        }

        if (fileNamesToKeep == null) {
            fileNamesToKeep = Collections.emptyList();
        }

        log.info("시설 ID: {}의 이미지 업데이트", facility.getId());
        log.info("유지할 파일명 목록: {}", fileNamesToKeep);
        log.info("새 이미지 개수: {}", newImages.size());

        List<File> updatedFiles = new ArrayList<>();

        // 기존 이미지
        List<FacilityPhoto> existingPhotos = facilityPhotoDao.findByFacilityId(facility.getId());
        log.info("기존 이미지 수: {}", existingPhotos);

        if (existingPhotos != null && !existingPhotos.isEmpty()) {
            // 삭제할 이미지와 유지할 이미지 분리
            for (FacilityPhoto photo : existingPhotos) {
                File file = photo.getFile();
                String path = file.getPath();

                String fileName = extractFileName(path);

                if (fileNamesToKeep.contains(fileName)) {
                    // 유지할 이미지
                    updatedFiles.add(photo.getFile());
                    log.info("이미지 유지: fileName: {}, fileId: {}", fileName, file.getId());
                } else {
                    // 삭제할 이미지
                    try {
                        log.info("이미지 삭제 처리 fileId: {}, fileName: {}", file.getId(), fileName);
                        facilityPhotoDao.delete(photo);
                        fileDao.delete(file);
                        storageService.delete(path);
                    } catch (StorageServiceException e) {
                        log.warn("이미지 삭제 중 오류 발생: {}", e.getMessage());
                    }
                }
            }
        }

        // 새 이미지 추가
        if (!newImages.isEmpty()) {
            for (MultipartFile imageFile : newImages) {
                if (imageFile != null && !imageFile.isEmpty()) {
                    try {
                        // 파일 저장
                        File savedFile = fileService.save(
                                imageFile.getOriginalFilename(),
                                "facility",
                                File.FileType.PHOTO);

                        // 물리적 파일 업로드
                        try (InputStream is = imageFile.getInputStream()) {
                            storageService.upload(savedFile.getPath(), is);
                        }

                        // 시설-이미지 연결 생성
                        FacilityPhoto newPhoto = FacilityPhoto.of(savedFile, facility);

                        // DB에 저장
                        facilityPhotoDao.save(newPhoto);

                        // 결과 목록에 추가
                        updatedFiles.add(savedFile);
                        log.info("새 이미지 추가 완료: fileId={}", savedFile.getId());
                    } catch (Exception | StorageServiceException e) {
                        log.error("새 이미지 추가 중 오류 발생: {}", e.getMessage());
                    }
                }
            }
        }

        log.info("이미지 업데이트 완료 - 최종 이미지 개수: {}", updatedFiles.size());
        return updatedFiles;
    }

    // 파일명 추출 헬퍼 메서드
    private String extractFileName(String path) {
        // uploads/facility/filename.jpg 형태에서 filename.jpg만 추출
        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex >= 0 && lastSlashIndex < path.length() - 1) {
            return path.substring(lastSlashIndex + 1);
        }
        return path; // 슬래시가 없으면 전체 경로 반환
    }

    private void validateImage(MultipartFile image) {
        long maxSize = 5 * 1024 * 1024;
        if (image.getSize() > maxSize) {
            throw new IllegalArgumentException("이미지 크기가 5MB를 초과합니다");
        }
    }

    private FacilityAddResponseDto convertToResponseDto(Facility facility, List<File> files) {
        Double starPoint = facility.getReviewCount() == 0
                ? 0.0
                : (double) facility.getTotalStarPoint() / facility.getReviewCount();

        FacilityAddResponseDto responseDto = FacilityAddResponseDto.builder()
                .id(facility.getId())
                .name(facility.getName())
                .facilityType(facility.getFacilityType().getName())
                .tel(facility.getTel())
                .address(facility.getAddress())
                .detailAddress(facility.getDetailAddress())
                .comment(facility.getComment())
                .latitude(facility.getLatitude())
                .longitude(facility.getLongitude())
                .starPoint(starPoint)
                .reviewCount(facility.getReviewCount())
                .createdAt(facility.getCreatedAt())
                .build();

        List<FacilityTimetable> timetables = facilityTimetableDao.findByFacilityId(facility.getId());

        List<FacilityAddResponseDto.FacilityTimetableDto> timetableDtos = timetables.stream()
                .map(timetable -> {
                    String openTimeStr = timetable.getOpenTime() != null ? timetable.getOpenTime().toString().substring(0, 5) : null;
                    String closeTimeStr = timetable.getCloseTime() != null ? timetable.getCloseTime().toString().substring(0, 5) : null;

                    return FacilityAddResponseDto.FacilityTimetableDto.builder()
                            .day(timetable.getDay().getValue())
                            .openTime(openTimeStr)
                            .closeTime(closeTimeStr)
                            .build();
                })
                .collect(Collectors.toList());

        responseDto.setTimetables(timetableDtos);

        List<FacilityAddResponseDto.FacilityImageDto> imageDtos = files.stream()
                .peek(file -> log.info("파일 ID: {}, 파일 경로: {}", file.getId(), file.getPath())) // 파일 정보 확인
                .map(file -> FacilityAddResponseDto.FacilityImageDto.builder()
                        .id(file.getId())
                        .url(storageService.generatePresignedUrl(file.getPath()))
                        .build())
                .collect(Collectors.toList());

        responseDto.setImages(imageDtos);

        return responseDto;
    }

    private Page<FacilityResponseDto> convertToDtoPage(Page<Facility> facilities, Pageable pageable) {
        List<FacilityResponseDto> facilityDtos = facilities.getContent().stream()
                .map(facility -> {
                    FacilityResponseDto dto = FacilityResponseDto.fromEntity(facility);

                    // 이미지가 있는 경우 URL 가져오기
                    if (facility.getPhotos() != null && !facility.getPhotos().isEmpty()) {
                        FacilityPhoto photo = facility.getPhotos().get(0);
                        String imageUrl = storageService.generatePresignedUrl(photo.getFile().getPath());
                        dto.setImagePath(imageUrl);
                    }

                    return dto;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(facilityDtos, pageable, facilities.getTotalElements());
    }

    // Entity를 DTO로 변환
    private FacilityResponseDto convertToDto(Facility facility) {
        FacilityResponseDto dto = FacilityResponseDto.fromEntity(facility);

        // 시설 대표 이미지가 있으면 Presigned URL 생성
        if (facility.getPhotos() != null && !facility.getPhotos().isEmpty()) {
            // 모든 사진에 대한 URL
            List<String> imageUrls = facility.getPhotos().stream()
                    .map(photo -> storageService.generatePresignedUrl(photo.getFile().getPath()))
                    .collect(Collectors.toList());
            dto.setImagePaths(imageUrls);
            log.info("imageUrls: {}", imageUrls);

            if (!imageUrls.isEmpty()) {
                dto.setImagePath(imageUrls.get(0));
            }
        }

        // 영업 시간표 조회 및 설정
        List<FacilityTimetable> timetables = facilityTimetableDao.findByFacilityId(facility.getId());

        // 요일별 영업시간 맵으로 변환
        Map<String, FacilityResponseDto.OpeningHoursDto> openingHours = new HashMap<>();

        // DB에서 가져온 영업시간으로 업데이트
        for (FacilityTimetable timetable : timetables) {
            String day = timetable.getDay().toString();

            FacilityResponseDto.OpeningHoursDto hoursDto = new FacilityResponseDto.OpeningHoursDto(
                    timetable.getOpenTime() != null ? timetable.getOpenTime().toString() : null,
                    timetable.getCloseTime() != null ? timetable.getCloseTime().toString() : null,
                    timetable.getOpenTime() != null
            );

            openingHours.put(day, hoursDto);
        }
        dto.setOpeningHours(openingHours);

        return dto;
    }

    public Slice<FacilityCardResponseDto> getFacilityCardsForReserve(FacilityList requestDto) {
        String day = requestDto.getDay();
        FacilityTimetable.Day dayEnum = FacilityTimetable.Day.valueOf(day.toUpperCase());
        String category = requestDto.getCategory();
        double lat = requestDto.getUserLatitude();
        double lng = requestDto.getUserLongitude();
        Pageable pageable = PageRequest.of(requestDto.getPage(), requestDto.getSize());

        Slice<FacilityWithDistanceProjection> list;
        if (requestDto.getSortBy().equals("distance")) {
            list = facilityDao.findByCategoryWithSortByDistance(lng, lat, category, pageable);
        } else if (requestDto.getSortBy().equals("starPoint")) {
            list = facilityDao.findByCategoryWithSortByStarPoint(lng, lat, category, pageable);
        } else {
            throw new IllegalArgumentException("유효하지 않은 정렬 기준입니다.");
        }

        List<Integer> facilityIds = list.getContent().stream()
                .map(FacilityWithDistanceProjection::getId)
                .collect(Collectors.toList());

        List<FacilityTimetable> timetables = facilityTimetableDao.findByFacility_IdInAndDay(facilityIds, dayEnum);
        List<ThumbnailForCardDto> thumbnails = facilityPhotoDao.findThumbnailPathByFacilityIds(facilityIds);

        // Map으로 변환해서 빠르게 찾을 수 있도록
        Map<Integer, FacilityTimetable> timetableMap = timetables.stream()
                .collect(Collectors.toMap(t -> t.getFacility().getId(), Function.identity()));
        Map<Integer, ThumbnailForCardDto> thumbnailMap = thumbnails.stream()
                .collect(Collectors.toMap(ThumbnailForCardDto::getFacilityId, Function.identity()));

        LocalTime now = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        List<FacilityCardResponseDto> mappedList = list.getContent().stream()
                .map(f -> {
                    FacilityTimetable timetable = timetableMap.get(f.getId());
                    ThumbnailForCardDto thumbnail = thumbnailMap.get(f.getId());
                    boolean isOpened = false;
                    String openTimeRange = "휴무";

                    if (timetable != null && timetable.getOpenTime() != null && timetable.getCloseTime() != null) {
                        LocalTime openTime = timetable.getOpenTime().toLocalTime();
                        LocalTime closeTime = timetable.getCloseTime().toLocalTime();
                        openTimeRange = openTime.format(formatter) + " - " + closeTime.format(formatter);

                        isOpened = (openTime.isBefore(closeTime))
                                ? !now.isBefore(openTime) && !now.isAfter(closeTime)
                                : !now.isBefore(openTime) || !now.isAfter(closeTime);
                    }
                    log.info("facilityName: {}, openTime: {}, closeTime: {}, openTimeRange: {}, isOpened: {}, image: {}", f.getName(), timetable != null ? timetable.getOpenTime() : null, timetable != null ? timetable.getCloseTime() : null, openTimeRange, isOpened, thumbnail != null ? storageService.generatePresignedUrl(thumbnail.getPath()) : null);

                    Integer totalStarPoint = f.getTotalStarPoint();
                    Integer reviewCount = f.getReviewCount();
                    double rating;
                    if (totalStarPoint == 0 || reviewCount == 0) {
                        rating = 0.0;
                    } else {
                        rating = (double) totalStarPoint / reviewCount;
                    }
                    return FacilityCardResponseDto.builder()
                            .id(f.getId())
                            .category(f.getCategory())
                            .name(f.getName())
                            .rating(rating)
                            .reviewCount(f.getReviewCount())
                            .distance(f.getDistance())
                            .tel(f.getTel())
                            .address(f.getAddress())
                            .openTimeRange(openTimeRange)
                            .isOpened(isOpened)
                            .image(thumbnail != null ? storageService.generatePresignedUrl(thumbnail.getPath()) : null)
                            .build();
                })
                .collect(Collectors.toList());

        return new SliceImpl<>(mappedList, list.getPageable(), list.hasNext());
    }

    @Transactional
    public void insertReview(ReviewInsertRequestDto requestDto, Integer userId, MultipartFile image) {

        User user = userDao.findById(userId).orElseThrow(() -> new EntityNotFoundException("유저 없음"));
        log.info("유저: {}", user);

        Reserve reserve = reserveDao.findById(requestDto.getReserveId()).orElseThrow(() -> new CustomException() {
            @Override
            public HttpStatus getStatus() {
                return HttpStatus.BAD_REQUEST;
            }

            @Override
            public String getMessage() {
                return "찾을 수 없는 예약 정보입니다.";
            }
        });

        if (reserve.getReview() != null) {
            throw new CustomException() {
                @Override
                public HttpStatus getStatus() {
                    return HttpStatus.BAD_REQUEST;
                }

                @Override
                public String getMessage() {
                    return "리뷰는 한번만 남길 수 있습니다";
                }
            };
        }

        // 시설 찾기
        Facility facility = facilityDao.findById(requestDto.getFacilityId()).orElseThrow(() -> new EntityNotFoundException("시설 없음"));
        log.info("바뀌기 전 총 별점: {}", facility.getTotalStarPoint());

        // 리뷰 저장
        Review review = Review.builder()
                .user(user)
                .facility(facility)
                .comment(requestDto.getComment())
                .starPoint(requestDto.getStarPoint())
                .build();
        Review saved = reviewDao.save(review);
        log.info("리뷰: {}", saved);

        reserve.updateReview(saved);
        reserveDao.save(reserve);

        // 시설 별점 통계 업데이트
        facility.updateTotalStarPoint(review.getStarPoint());
        facility.updateReviewCount();
        facilityDao.save(facility);
        log.info("바뀐 후 총 별점: {}", facility.getTotalStarPoint());

        if (image != null && !image.isEmpty()) {
            // 리뷰 이미지 저장
            File file = fileService.save(image.getOriginalFilename(), "review", File.FileType.PHOTO);
            log.info("파일 저장 완료");

            // 시설-리뷰 이미지 연결 생성
            ReviewPhoto reviewPhoto = ReviewPhoto.of(file, review);
            reviewPhotoDao.save(reviewPhoto);
            log.info("시설-리뷰 이미지 연결 생성 완료");

            try (InputStream is = image.getInputStream()) {
            storageService.upload(file.getPath(), is);

            } catch (IOException | StorageServiceException e) {
                throw new RuntimeException("파일 업로드 중 오류 발생: " + e);
            }
        }
    }

    @Transactional
    public FacilityDetailDto updateReview(Integer reviewId, Integer userId, String comment, Integer starPoint, MultipartFile image)
            throws AccessDeniedException, StorageServiceException {

        Review review = reviewDao.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        if (!review.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("리뷰를 수정할 권한이 없습니다.");
        }

        log.info("수정 전 총 별점: {}", review.getFacility().getTotalStarPoint());

        Integer oldStarPoint = review.getStarPoint();
        if (!Objects.equals(oldStarPoint, starPoint)) {
            int diff = starPoint - oldStarPoint;
            review.getFacility().updateTotalStarPoint(diff);
            log.info("별점 변경 사항: {}", diff > 0 ? "+" + diff : diff);
        }

        review.update(comment, starPoint);

        // 이미지 삭제 또는 변경
        List<ReviewPhoto> oldPhotos = reviewPhotoDao.findByReviewId(review.getId());

        // ✅ 이미지 제거 요청인 경우
        if (image == null || image.isEmpty()) {
            if (!oldPhotos.isEmpty()) {
                for (ReviewPhoto photo : oldPhotos) {
                    storageService.delete(photo.getFile().getPath());
                }
                reviewPhotoDao.deleteAll(oldPhotos);
                fileDao.deleteAll(oldPhotos.stream().map(ReviewPhoto::getFile).collect(Collectors.toList()));
                log.info("기존 리뷰 이미지 삭제 완료");
            }
        }
        // ✅ 이미지 수정 요청인 경우
        else {
            // 기존 이미지 삭제
            if (!oldPhotos.isEmpty()) {
                for (ReviewPhoto photo : oldPhotos) {
                    storageService.delete(photo.getFile().getPath());
                }
                reviewPhotoDao.deleteAll(oldPhotos);
                fileDao.deleteAll(oldPhotos.stream().map(ReviewPhoto::getFile).collect(Collectors.toList()));
                log.info("기존 리뷰 이미지 삭제 완료");
            }

            // 새 이미지 저장
            try (InputStream is = image.getInputStream()) {
                File newFile = fileService.save(image.getOriginalFilename(), "review", File.FileType.PHOTO);
                storageService.upload(newFile.getPath(), is);
                ReviewPhoto newPhoto = ReviewPhoto.of(newFile, review);
                reviewPhotoDao.save(newPhoto);
                log.info("새 리뷰 이미지 저장 완료");
            } catch (IOException | StorageServiceException | NullPointerException e) {
                throw new RuntimeException("파일 업로드 중 오류 발생: " + e);
            }
        }

        FacilityResponseDto newFacility = getFacilityById(review.getFacility().getId());
        List<ReviewResponseDto> newReviews = getReviewDtos(review.getFacility().getId());
        List<Object[]> newRatingRatio = reviewDao.countReviewsByStarPoint(review.getFacility().getId());

        return new FacilityDetailDto(newFacility, newReviews, newRatingRatio, userId);
    }


    @Transactional
    public FacilityDetailDto deleteReview(Integer id, Integer userId) throws AccessDeniedException, StorageServiceException {
        Review review = reviewDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        if (!review.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("리뷰를 삭제할 권한이 없습니다.");
        }

        List<ReviewPhoto> reviewPhotos = reviewPhotoDao.findByReviewId(id);
        if (!reviewPhotos.isEmpty()) {
            try {
                for (ReviewPhoto photo : reviewPhotos) {
                    reviewPhotoDao.delete(photo);
                    fileDao.delete(photo.getFile());
                    storageService.delete(photo.getFile().getPath());
                }
                log.info("리뷰 이미지 삭제 완료");
            } catch (StorageServiceException e) {
                throw new StorageServiceException("리뷰 이미지 삭제 중 오류 발생: " + e.getMessage(), e);
            }
        }

        review.getFacility().updateTotalStarPoint(-review.getStarPoint());
        review.getFacility().discountReview();
        reviewDao.delete(review);
        log.info("리뷰 삭제 완료");

        FacilityResponseDto newFacility = getFacilityById(review.getFacility().getId());
        List<ReviewResponseDto> newReviews = getReviewDtos(review.getFacility().getId());
        List<Object[]> newRatingRatio = reviewDao.countReviewsByStarPoint(review.getFacility().getId());

        return new FacilityDetailDto(newFacility, newReviews, newRatingRatio, userId);

    }
}