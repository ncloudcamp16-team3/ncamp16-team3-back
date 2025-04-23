package tf.tailfriend.global.service;

import org.springframework.stereotype.Service;

@Service
public class DistanceFormatProviderImpl implements DistanceFormatProvider {

    public static final double EARTH_RADIUS_KM = 6371.0; // 지구 반지름 (km)
    /**
     * 첫번째 위경도와 두번째 위경도의 거리 차이를 구하는 메소드
     * @param lat1 위도1
     * @param lon1 경도1
     * @param lat2 위도2
     * @param lon2 경도2
     * @return 두 지점 사이의 거리 (단위: km)
     */
    @Override
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
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
}
