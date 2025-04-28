package tf.tailfriend.reserve.service;

import org.springframework.stereotype.Service;
import tf.tailfriend.global.service.DateTimeFormatProvider;
import tf.tailfriend.reserve.repository.ReserveDao;

@Service
public class ReserveService {

    private final ReserveDao reserveDao;
    private final DateTimeFormatProvider dateTimeFormatProvider;

    public ReserveService(ReserveDao reserveDao, DateTimeFormatProvider dateTimeFormatProvider) {
        this.reserveDao = reserveDao;
        this.dateTimeFormatProvider = dateTimeFormatProvider;
    }


}