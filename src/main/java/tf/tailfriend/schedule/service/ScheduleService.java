package tf.tailfriend.schedule.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tf.tailfriend.schedule.entity.dto.ScheduleDTO.*;
import tf.tailfriend.schedule.repository.ScheduleDao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleService {

    private final ScheduleDao scheduleDao;

    @Transactional(readOnly = true)
    public List<ScheduleGetDTO> getAllSchedules(Integer id) {
        return scheduleDao.findById(id)
                .stream()
                .map(ScheduleGetDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ScheduleGetDTO> getOneSchedules(LocalDate selectedDate, Integer id) {
        return scheduleDao.findById(id)
                .stream()
                .map(ScheduleGetDTO::new)
                .filter(dto -> dto.getDateList().contains(selectedDate)) // 선택 날짜 포함 여부 확인
                .collect(Collectors.toList());
    }
}
