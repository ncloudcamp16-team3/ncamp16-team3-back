package tf.tailfriend.notification.scheduler;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tf.tailfriend.notification.config.NotificationMessageProducer;
import tf.tailfriend.notification.entity.Notification;
import tf.tailfriend.notification.entity.NotificationType;
import tf.tailfriend.notification.entity.UserFcm;
import tf.tailfriend.notification.entity.dto.NotificationDto;
import tf.tailfriend.notification.repository.NotificationDao;
import tf.tailfriend.notification.repository.NotificationTypeDao;
import tf.tailfriend.notification.repository.UserFcmDao;
import tf.tailfriend.notification.service.NotificationService;
import tf.tailfriend.reserve.entity.Reserve;
import tf.tailfriend.reserve.repository.ReserveDao;
import tf.tailfriend.schedule.entity.Schedule;
import tf.tailfriend.schedule.repository.ScheduleDao;
import tf.tailfriend.user.entity.User;
import tf.tailfriend.user.repository.UserDao;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final ScheduleDao scheduleDao;
    private final ReserveDao reserveDao;
    private final NotificationService notificationService;
    private final NotificationDao notificationDao;
    private final UserDao userDao;
    private final NotificationTypeDao notificationTypeDao;
    private final NotificationMessageProducer NotificationMessageProducer;
    private final UserFcmDao userFcmDao;

    @PostConstruct
    public void init() {
        System.out.println("🔔 NotificationScheduler 초기화됨");
    }



    @Transactional
    @Scheduled(fixedRate = 10000) // 1분마다 실행
    public void sendScheduledNotifications() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tenMinutesLater = now.plusMinutes(10);

        log.debug("🔄 NotificationScheduler 실행됨: 현재 시간 = {}, 10분 후 = {}", now, tenMinutesLater);


        // 예약 알림 처리 (notifyTypeId = 3)
        List<Reserve> upcomingReserves = reserveDao.findByEntryTimeBetween(now, tenMinutesLater);

        if (upcomingReserves.isEmpty()) {
            log.debug("📌 10분 후 시작 예정인 예약 없음.");
        } else {
            log.debug("📌 10분 후 시작 예정인 예약 개수: {}", upcomingReserves.size());
        }

        for (Reserve reserve : upcomingReserves) {
            sendNotificationAndSaveLog(
                    reserve.getUser().getId(),
                    3,
                    String.valueOf(reserve.getId()),
                    "📌 예약 알림 전송 완료: userId={}, 시설명={}",
                    reserve.getUser().getId(),
                    reserve.getFacility().getName(),
                    "❌ 예약 알림 전송 실패: reserveId=" + reserve.getId()
            );
        }

        // 일정 알림 처리 (notifyTypeId = 4)
        List<Schedule> upcomingSchedules = scheduleDao.findByStartDateBetween(now, tenMinutesLater);

        if (upcomingSchedules.isEmpty()) {
            log.debug("📅 10분 후 시작 예정인 일정 없음.");
        } else {
            log.debug("📅 10분 후 시작 예정인 일정 개수: {}", upcomingSchedules.size());
        }


            for (Schedule schedule : upcomingSchedules) {
            sendNotificationAndSaveLog(
                    schedule.getUser().getId(),
                    4,
                    String.valueOf(schedule.getId()),
                    "📅 일정 알림 전송 및 저장 완료: userId={}, 일정명={}",
                    schedule.getUser().getId(),
                    schedule.getTitle(),
                    "❌ 일정 알림 전송 실패: scheduleId=" + schedule.getId()
            );
        }
    }

    private void sendNotificationAndSaveLog(Integer userId, Integer notifyTypeId, String content,
                                            String successLogFormat, Object arg1, Object arg2, String errorLogMsg) {
        try {
            log.debug("🔍 알림 전송 로직 시작: userId={}, notifyTypeId={}, content={}", userId, notifyTypeId, content);

            // 1. FCM 토큰 조회
            UserFcm userFcm = userFcmDao.findByUserId(userId).orElseThrow();
            log.debug("📱 FCM 토큰 조회 성공: fcmToken={}", userFcm.getFcmToken());

            // 2. DTO 생성 및 RabbitMQ 전송
            NotificationDto dto = NotificationDto.builder()
                    .userId(userId)
                    .notifyTypeId(notifyTypeId)
                    .content(content)
                    .fcmToken(userFcm.getFcmToken())
                    .build();

            log.debug("📦 RabbitMQ 전송 전 DTO: {}", dto);
            NotificationMessageProducer.sendNotification(dto);
            log.info("🚀 RabbitMQ 전송 완료");

            // 3. DB 저장
            User user = userDao.findById(userId).orElseThrow();
            NotificationType type = notificationTypeDao.findById(notifyTypeId).orElseThrow();

            Notification notification = Notification.builder()
                    .user(user)
                    .notificationType(type)
                    .content(content)
                    .readStatus(false)
                    .build();

            log.debug("📝 DB 저장 전 Notification 객체: {}", notification);
            notificationDao.save(notification);
            log.info("💾 알림 DB 저장 완료");

            // 4. 완료 로그
            log.info(successLogFormat, arg1, arg2);
        } catch (Exception e) {
            log.error(errorLogMsg, e);
        }
    }


}