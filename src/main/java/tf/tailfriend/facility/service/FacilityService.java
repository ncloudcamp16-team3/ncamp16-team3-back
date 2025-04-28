package tf.tailfriend.facility.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tf.tailfriend.facility.dto.FacilityAddResponseDto;
import tf.tailfriend.facility.dto.FacilityRequestDto;
import tf.tailfriend.facility.dto.FacilityResponseDto;
import tf.tailfriend.facility.entity.Facility;
import tf.tailfriend.facility.entity.FacilityPhoto;
import tf.tailfriend.facility.entity.FacilityTimetable;
import tf.tailfriend.facility.entity.FacilityType;
import tf.tailfriend.facility.repository.FacilityDao;
import tf.tailfriend.facility.repository.FacilityPhotoDao;
import tf.tailfriend.facility.repository.FacilityTimetableDao;
import tf.tailfriend.facility.repository.FacilityTypeDao;
import tf.tailfriend.file.entity.File;
import tf.tailfriend.file.service.FileService;
import tf.tailfriend.global.service.StorageService;
import tf.tailfriend.global.service.StorageServiceException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.*;
import tf.tailfriend.facility.entity.FacilityTimetable.Day;
import tf.tailfriend.facility.entity.dto.ResponseForReserve.FacilityCard;
import tf.tailfriend.global.service.DateTimeFormatProvider;
import tf.tailfriend.reserve.dto.RequestForFacility.FacilityList;


@Slf4j
@Service
@RequiredArgsConstructor
public class FacilityService {

    private final FacilityDao facilityDao;
    private final FacilityTypeDao facilityTypeDao;
    private final FacilityTimetableDao facilityTimetableDao;
    private final FacilityPhotoDao facilityPhotoDao;
    private final StorageService storageService;
    private final FileService fileService;

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
        FacilityType facilityType = facilityTypeDao.findById(Integer.valueOf(requestDto.getFacilityTypeId()))
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
                .starPoint(0.0)
                .createdAt(LocalDateTime.now())
                .build();

        Facility savedFacility = facilityDao.save(facility);
        log.info("saved facility: {}", savedFacility);

        if (requestDto.getOpenTime() != null && requestDto.getCloseTime() != null) {
            saveDailyTimetables(savedFacility, requestDto.getOpenTime(), requestDto.getCloseTime());
        } else if (requestDto.getOpenTimes() != null && requestDto.getCloseTimes() != null) {
            saveWeeklyTimetables(
                    savedFacility,
                    requestDto.getOpenTimes(),
                    requestDto.getCloseTimes(),
                    requestDto.getOpenDays()
            );
        }

        List<File> savedFiles = saveImages(savedFacility, images);

        return convertToResponseDto(savedFacility, savedFiles);
    }

    private FacilityAddResponseDto convertToResponseDto(Facility facility, List<File> files) {
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
                .starPoint(facility.getStarPoint())
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

        List<String> imageUrls = files.stream()
                .map(file -> {
                    String presignedUrl = storageService.generatePresignedUrl(file.getPath());
                    return presignedUrl;
                })
                .collect(Collectors.toList());

        responseDto.setImageUrls(imageUrls);

        return responseDto;
    }

    private void saveDailyTimetables(Facility facility, String openTimeStr, String closeTimeStr) {

        log.info("openTimeStr: {}, closeTimeStr: {}", openTimeStr, closeTimeStr);

        if (openTimeStr == null || closeTimeStr == null) {
            log.warn("시간 값이 null입니다. 시간표를 저장하지 않습니다.");
            return;
        }
        log.info("Time.valueOf(openTimeStr): {}, Time.valueOf(closeTimeStr): {}", Time.valueOf(openTimeStr), Time.valueOf(closeTimeStr));
        // HH:MM 형식을 HH:MM:SS 형식으로 변환
        String openTimeWithSeconds = openTimeStr + ":00";
        String closeTimeWithSeconds = closeTimeStr + ":00";

        log.info("변환된 시간 - openTime: {}, closeTime: {}", openTimeWithSeconds, closeTimeWithSeconds);

        try {
            // 시간 변환 시도
            Time openTime = Time.valueOf(openTimeWithSeconds);
            Time closeTime = Time.valueOf(closeTimeWithSeconds);

            // 요일별로 같은 시간 설정
            for (FacilityTimetable.Day day : FacilityTimetable.Day.values()) {
                FacilityTimetable timetable = FacilityTimetable.builder()
                        .facility(facility)
                        .day(day)
                        .openTime(openTime)
                        .closeTime(closeTime)
                        .build();

                facilityTimetableDao.save(timetable);
            }
        } catch (IllegalArgumentException e) {
            log.error("시간 형식 변환 중 오류 발생: {}", e.getMessage());
            // 오류 처리 - 예외를 던지거나 기본값 사용
        }
    }

    private void saveWeeklyTimetables(Facility facility, Map<String, String> openTimes, Map<String, String> closeTimes, Map<String, Boolean> openDays) {
        for (FacilityTimetable.Day day : FacilityTimetable.Day.values()) {
            String dayName = day.getValue();

            boolean isOpen = openDays != null ? openDays.get(dayName) : true;
            if (!isOpen) {
                log.info("시설 ID {}의 요일 {} 휴무일 처리됨", facility.getId(), dayName);
                continue;
            }

            String openTimeStr = openTimes.get(dayName);
            String closeTimeStr = closeTimes.get(dayName);

            if (openTimeStr == null || closeTimeStr == null) {
                log.warn("시설 ID {}의 요일 {} 영업시간 정보 누락, 기본값 적용", facility.getId(), dayName);
                openTimeStr = "09:00";
                closeTimeStr = "18:00";
            }

            Time openTime = Time.valueOf(openTimeStr);
            Time closeTime = Time.valueOf(closeTimeStr);

            FacilityTimetable timetable = FacilityTimetable.builder()
                    .facility(facility)
                    .day(day)
                    .openTime(openTime)
                    .closeTime(closeTime)
                    .build();

            facilityTimetableDao.save(timetable);
        }
        log.info("시설 ID {} 에 요일별 영업시간 저장 완료", facility.getId());
    }

    private List<File> saveImages(Facility facility, List<MultipartFile> images) {
        List<File> savedFiles = new ArrayList<>();

        if (images == null || images.isEmpty()) {
            log.info("시설 ID {}에 업로드된 이미지 없음", facility.getId());
            return  savedFiles;
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

    private void validateImage(MultipartFile image) {
        long maxSize = 5 * 1024 * 1024;
        if (image.getSize() > maxSize) {
            throw new IllegalArgumentException("이미지 크기가 5MB를 초과합니다");
        }
    }

    @Transactional
    public void deleteFacilityById(Integer facilityId) {
        Facility facility = facilityDao.findById(facilityId)
                .orElseThrow(() -> new IllegalArgumentException("등록된 업체가 없습니다"));

        facilityDao.delete(facility);
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
            // 첫 번째 사진 URL 생성 (대표 이미지)
            String imageUrl = storageService.generatePresignedUrl(
                    facility.getPhotos().iterator().next().getFile().getPath());
            dto.setImagePath(imageUrl);
        }

        return dto;
    }

    public Slice<FacilityCard> getFacilityCardsForReserve(FacilityList requestDto) {
        Sort sort = Sort.by(Sort.Direction.DESC, requestDto.getSortBy());
        Day day = switch (requestDto.getDay()) {
            case "MON" -> Day.MONDAY;
            case "TUE" -> Day.TUESDAY;
            case "WED" -> Day.WEDNESDAY;
            case "THU" -> Day.THURSDAY;
            case "FRI" -> Day.FRIDAY;
            case "SAT" -> Day.SATURDAY;
            case "SUN" -> Day.SUNDAY;
            default -> throw new IllegalArgumentException("Invalid day: " + requestDto.getDay());
        };
        String category = requestDto.getCategory();
        double lat = requestDto.getUserLatitude();
        double lng = requestDto.getUserLongitude();
        Pageable pageable = PageRequest.of(requestDto.getPage(), requestDto.getSize(), sort);

        // 먼저 시설 정보만 조회
        Slice<FacilityCard> facilities = facilityDao.findByCategoryWithFacilityTypeAndThumbnail(category, day, lat, lng, pageable);

        return facilities;
    }

}
