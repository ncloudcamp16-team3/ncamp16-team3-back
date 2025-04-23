package tf.tailfriend.reserve.service;

import org.springframework.stereotype.Service;
import tf.tailfriend.global.service.DateTimeFormatProvider;
import tf.tailfriend.reserve.repository.ReserveDao;
import tf.tailfriend.global.service.DistanceFormatProvider;

@Service
public class ReserveService {

    private final ReserveDao reserveDao;
    private final DistanceFormatProvider distanceFormatProvider;
    private final DateTimeFormatProvider dateTimeFormatProvider;

    public ReserveService(ReserveDao reserveDao, DistanceFormatProvider distanceFormatProvider, DateTimeFormatProvider dateTimeFormatProvider) {
        this.reserveDao = reserveDao;
        this.distanceFormatProvider = distanceFormatProvider;
        this.dateTimeFormatProvider = dateTimeFormatProvider;
    }


}