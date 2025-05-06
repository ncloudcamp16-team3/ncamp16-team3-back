package tf.tailfriend.reserve.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tf.tailfriend.facility.entity.Facility;
import tf.tailfriend.facility.repository.FacilityDao;
import tf.tailfriend.global.service.DateTimeFormatProvider;
import tf.tailfriend.global.service.RedisService;
import tf.tailfriend.reserve.dto.ReserveRequestDto;
import tf.tailfriend.reserve.entity.Reserve;
import tf.tailfriend.reserve.repository.ReserveDao;
import tf.tailfriend.user.entity.User;
import tf.tailfriend.user.repository.UserDao;

@RequiredArgsConstructor
@Service
public class ReserveService {

    private final ReserveDao reserveDao;
    private final DateTimeFormatProvider dateTimeFormatProvider;
    private final UserDao userDao;
    private final RedisService redisService;
    private final FacilityDao facilityDao;

    @Transactional
    public Reserve saveReserveAfterPayment(String merchantPayKey) {
        ReserveRequestDto dto = redisService.getTempReserve(merchantPayKey); // ✅ prefix 없이 그대로 사용


        if (dto == null) throw new IllegalArgumentException("Redis에 예약 정보 없음");

        User user = userDao.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        Facility facility = facilityDao.findById(dto.getFacilityId())
                .orElseThrow(() -> new IllegalArgumentException("시설 없음"));

        Reserve reserve = Reserve.builder()
                .user(user)
                .facility(facility)
                .entryTime(dto.getEntryTime())
                .exitTime(dto.getExitTime())
                .amount(dto.getAmount())
                .reserveStatus(true)
                .build();

        Reserve saved = reserveDao.save(reserve);
        redisService.deleteTempReserve("reserve:" + merchantPayKey);

        return saved;
    }


}