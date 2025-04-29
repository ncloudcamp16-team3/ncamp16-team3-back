package tf.tailfriend.schedule.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tf.tailfriend.notification.entity.UserFcm;
import tf.tailfriend.notification.entity.dto.NotificationDto;
import tf.tailfriend.notification.service.NotificationService;
import tf.tailfriend.schedule.entity.Schedule;
import tf.tailfriend.schedule.entity.dto.ScheduleDTO.*;
import tf.tailfriend.schedule.repository.ScheduleDao;
import tf.tailfriend.user.entity.User;
import tf.tailfriend.user.repository.UserDao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleService {

    private final ScheduleDao scheduleDao;
    private final UserDao userDao;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<ScheduleGetDTO> getAllSchedules(Integer userId) {
        return scheduleDao.findByUserId(userId)
                .stream()
                .map(ScheduleGetDTO::new)
                .collect(Collectors.toList());
    }



    public void postSchedule(SchedulePostDTO dto) {

        User user = userDao.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자 ID입니다."));

        Schedule schedule = Schedule.builder()
                .user(user)
                .title(dto.getTitle())
                .content(dto.getContent())
                .startDate(dto.getStartDate())
                .address(dto.getAddress())
                .endDate(dto.getEndDate())
                .longitude(dto.getLongitude())
                .latitude(dto.getLatitude())
                .build();

        scheduleDao.save(schedule);

        NotificationDto notification = new NotificationDto();
        notification.setUserId(dto.getUserId());
        notification.setContent("일정이 추가되었습니다."); // 알림 내용
        notification.setNotifyTypeId(3);
        notification.setFcmToken(dto.getFcmToken());

        notificationService.sendNotification(notification);
    }

    public void putSchedule(SchedulePutDTO dto) {

        User user = userDao.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자 ID입니다."));

        Schedule schedule = Schedule.builder()
                .id(dto.getId())
                .user(user)
                .title(dto.getTitle())
                .content(dto.getContent())
                .startDate(dto.getStartDate())
                .address(dto.getAddress())
                .endDate(dto.getEndDate())
                .longitude(dto.getLongitude())
                .latitude(dto.getLatitude())
                .build();

        scheduleDao.save(schedule);
    }

    public void deleteSchedule(Integer id) {
        scheduleDao.deleteById(id);
    }


}
