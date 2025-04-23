package tf.tailfriend.facility.service;

import org.springframework.data.domain.Slice;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import tf.tailfriend.facility.dto.FacilityCardForReserve;
import tf.tailfriend.facility.entity.Facility;
import tf.tailfriend.facility.repository.FacilityDao;
import tf.tailfriend.global.service.DateTimeFormatProvider;
import tf.tailfriend.global.service.DistanceFormatProvider;
import tf.tailfriend.reserve.dto.FacilityListRequestDto;

@Service
@RequiredArgsConstructor
public class FacilityService {

    private final FacilityDao facilityRepository;
    private final DistanceFormatProvider distanceFormatProvider;
    private final DateTimeFormatProvider dateTimeFormatProvider;

    public Slice<FacilityCardForReserve> getFacilityCardsForReserve(FacilityListRequestDto requestDto) {
        Sort sort = Sort.by(Sort.Direction.DESC, requestDto.getSortBy());
        String category = requestDto.getCategory();
        Pageable pageable = PageRequest.of(requestDto.getPage(), requestDto.getSize(), sort);

        Slice<Facility> facilities = facilityRepository.findAllFacilitiesForReserve(category, pageable);

        return facilities.map(facility ->
                FacilityCardForReserve.fromEntity(
                        facility,
                        requestDto.getUserLatitude(),
                        requestDto.getUserLongitude(),
                        distanceFormatProvider,
                        dateTimeFormatProvider
                )
        );
    }
}
