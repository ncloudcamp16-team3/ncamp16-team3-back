package tf.tailfriend.schedule.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tf.tailfriend.reserve.repository.ReserveDao;
import tf.tailfriend.schedule.entity.dto.CalendarReserveDTO;
import tf.tailfriend.schedule.entity.dto.EventDTO;
import tf.tailfriend.schedule.repository.EventDao;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CalendarReserveService {

    private final ReserveDao reserveDao;

    @Transactional(readOnly = true)
    public List<CalendarReserveDTO> getAllReserve(Integer userId) {
        return reserveDao.findByUserId(userId)
                .stream()
                .map(CalendarReserveDTO::new)
                .collect(Collectors.toList());
    }

}
