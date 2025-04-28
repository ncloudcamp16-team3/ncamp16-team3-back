package tf.tailfriend.facility.service;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import tf.tailfriend.facility.entity.Facility;
import tf.tailfriend.facility.entity.FacilityTimetable.Day;
import tf.tailfriend.facility.entity.dto.ResponseForReserve.FacilityCard;
import tf.tailfriend.facility.entity.dto.ResponseForReserve.FacilitySimpleDto;
import tf.tailfriend.facility.repository.FacilityDao;
import tf.tailfriend.global.service.DateTimeFormatProvider;
import tf.tailfriend.reserve.dto.RequestForFacility.FacilityList;

@Service
@RequiredArgsConstructor
public class FacilityService {

    private final FacilityDao facilityDao;
    private final DateTimeFormatProvider dateTimeFormatProvider;

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
