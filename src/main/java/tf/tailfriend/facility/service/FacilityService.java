package tf.tailfriend.facility.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tf.tailfriend.facility.dto.FacilityResponseDto;
import tf.tailfriend.facility.entity.Facility;
import tf.tailfriend.facility.entity.FacilityPhoto;
import tf.tailfriend.facility.entity.FacilityType;
import tf.tailfriend.facility.repository.FacilityDao;
import tf.tailfriend.facility.repository.FacilityTypeDao;
import tf.tailfriend.global.service.StorageService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacilityService {

    private final FacilityDao facilityDao;
    private final FacilityTypeDao facilityTypeDao;
    private final StorageService storageService;

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
}
