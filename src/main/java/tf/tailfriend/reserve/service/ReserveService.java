package tf.tailfriend.reserve.service;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import tf.tailfriend.reserve.dto.ListResponseDto;
import tf.tailfriend.reserve.dto.ReserveInfoResponseDto;
import tf.tailfriend.reserve.dto.ReserveListRequestDto;
import tf.tailfriend.reserve.entity.Reserve;
import tf.tailfriend.reserve.repository.ReserveDao;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
public class ReserveService {

    private final ReserveDao reserveDao;

    public ReserveService(ReserveDao reserveDao) {
        this.reserveDao = reserveDao;
    }
    private final DateTimeFormatter responseFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final DateTimeFormatter requestFormatter = DateTimeFormatter.ofPattern("E");
    private final LocalDateTime now = LocalDateTime.now();
    private final String nowDay = now.format(requestFormatter);
    private final String nowTime = now.format(responseFormatter);

    private static final double EARTH_RADIUS_KM = 6371.0; // 지구 반지름 (km)

    /**
     * 두 좌표 사이의 거리 (단위: km)
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double rLat1 = Math.toRadians(lat1);
        double rLat2 = Math.toRadians(lat2);

        double a = Math.pow(Math.sin(dLat / 2), 2) +
                Math.cos(rLat1) * Math.cos(rLat2) *
                        Math.pow(Math.sin(dLon / 2), 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    public ListResponseDto<ReserveInfoResponseDto> getReserveList(ReserveListRequestDto requestDto) {

            Slice<Reserve> reserves = reserveDao.getReserves(requestDto.getPage(), requestDto.getSize());

            for (Reserve reserve : reserves) {
                double distance = calculateDistance(
                        requestDto.getLatitude(),
                        requestDto.getLongitude(),
                        reserve.getFacility().getLatitude(),
                        reserve.getFacility().getLongitude());

                ReserveInfoResponseDto responseDto = ReserveInfoResponseDto.builder()
                        .id(reserve.getId())
                        .distance(distance)
                        .name(reserve.getFacility().getName())
                        .address(reserve.getFacility().getAddress())
                        .openTime(reserve.getFacility())



        }


        return
    }
}
