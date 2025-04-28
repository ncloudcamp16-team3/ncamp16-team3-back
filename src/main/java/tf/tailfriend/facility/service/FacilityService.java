package tf.tailfriend.facility.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
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
import tf.tailfriend.facility.entity.dto.ResponseForReserve.FacilityCard;
import tf.tailfriend.facility.entity.dto.ResponseForReserve.FacilityWithDistanceProjection;
import tf.tailfriend.facility.entity.dto.ResponseForReserve.Test;
import tf.tailfriend.facility.repository.*;
import tf.tailfriend.file.entity.File;
import tf.tailfriend.file.service.FileService;
import tf.tailfriend.global.service.StorageService;
import tf.tailfriend.global.service.StorageServiceException;
import tf.tailfriend.reserve.dto.RequestForFacility.FacilityList;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



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

        saveWeeklyTimetables(
                savedFacility,
                requestDto.getOpenTimes(),
                requestDto.getCloseTimes(),
                requestDto.getOpenDays()
        );

        List<File> savedFiles = saveImages(savedFacility, images);

        return convertToResponseDto(savedFacility, savedFiles);
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
            }
            else {
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

            if (!imageUrls.isEmpty()) {
                dto.setImagePath(imageUrls.get(0));
            }
        }

        return dto;
    }

    public Slice<FacilityCard> getFacilityCardsForReserve(FacilityList requestDto) {
        String day = requestDto.getDay();
        String category = requestDto.getCategory();
        double lat = requestDto.getUserLatitude();
        double lng = requestDto.getUserLongitude();
        Pageable pageable = PageRequest.of(requestDto.getPage(), requestDto.getSize());

        Slice<FacilityWithDistanceProjection> list = null;

        if (requestDto.getSortBy().equals("distance")) {
            list = facilityDao.findByCategoryWithSortByDistance(lng, lat, category, pageable);
        } else if (requestDto.getSortBy().equals("starPoint")) {
            list = facilityDao.findByCategoryWithSortByStarPoint(lng, lat, category, pageable);
        } else {
            throw new IllegalArgumentException("유효하지 않은 정렬 기준입니다.");
        }

        if (list.isEmpty()) {
            throw new IllegalArgumentException("시설을 찾을 수 없습니다.");
        }

        List<FacilityCard> mappedList = list.getContent().stream()
                .map(f -> FacilityCard.builder()
                .id(f.getId())
                .category(f.getCategory())
                .name(f.getName())
                .rating(f.getStarPoint())
                .reviewCount(f.getReviewCount())
                .distance(f.getDistance())
                .address(f.getAddress())
                .build())
                .collect(Collectors.toList());

        return new SliceImpl<>(mappedList, list.getPageable(), list.hasNext());
    }
}
