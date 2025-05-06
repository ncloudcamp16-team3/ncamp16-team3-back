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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final ScheduleDao scheduleDao;
    private final ReserveDao reserveDao;
    private final NotificationDao notificationDao;
    private final NotificationMessageProducer NotificationMessageProducer;
    private final UserFcmDao userFcmDao;


    @PostConstruct
    public void init() {
        System.out.println("🔔 NotificationScheduler 초기화됨");
    }


    @Transactional
    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void sendScheduledNotifications() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tenMinutesLater = now.plusMinutes(10);

        log.debug("🔄 NotificationScheduler 실행됨: 현재 시간 = {}, 10분 후 = {}", now, tenMinutesLater);

        boolean isLinux = System.getProperty("os.name").toLowerCase().contains("linux");
        boolean isDev = !isLinux; // 리눅스가 아니면 개발 환경

        // 예약 알림 처리 (notifyTypeId = 3)
        List<Reserve> upcomingReserves = reserveDao.findByEntryTimeBetween(now, tenMinutesLater);

        if (upcomingReserves.isEmpty()) {
            log.debug("📌 10분 후 시작 예정인 예약 없음.");
        } else {
            log.debug("📌 10분 후 시작 예정인 예약 개수: {}", upcomingReserves.size());
        }

        for (Reserve reserve : upcomingReserves) {
            String formattedCreatedAt = reserve.getEntryTime()
                    .atZone(ZoneId.of("Asia/Seoul"))
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            sendNotificationAndSaveLog(
                    reserve.getUser().getId(),
                    3,
                    String.valueOf(reserve.getId()),
                    formattedCreatedAt,
                    "📌 예약 알림 전송 완료: userId={}, 시설명={}",
                    reserve.getUser().getId(),
                    reserve.getFacility().getName(),
                    "❌ 예약 알림 전송 실패: reserveId=" + reserve.getId(),
                    isDev
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
            String formattedCreatedAt = schedule.getStartDate()
                    .atZone(ZoneId.of("Asia/Seoul"))
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            sendNotificationAndSaveLog(
                    schedule.getUser().getId(),
                    4,
                    String.valueOf(schedule.getId()),
                    formattedCreatedAt,
                    "📅 일정 알림 전송 및 저장 완료: userId={}, 일정명={}",
                    schedule.getUser().getId(),
                    schedule.getTitle(),
                    "❌ 일정 알림 전송 실패: scheduleId=" + schedule.getId(),
                    isDev
            );
        }
    }

    private String generateMessageId(Integer userId, Integer notifyTypeId, String scheduleStartDate, String content) {
        // 예시로 userId, notifyTypeId, content를 조합하여 messageId를 생성
        return String.format("%d-%d-%d-%s", userId, notifyTypeId, content.hashCode(), scheduleStartDate, content);
    }

    public void sendNotificationAndSaveLog(Integer userId, Integer notifyTypeId, String content, String scheduleStartDate,
                                           String successLogFormat, Object arg1, Object arg2, String errorLogMsg,
                                           boolean isDev) {

        try {
            log.debug("🔍 알림 전송 로직 시작: userId={}, notifyTypeId={}, content={}", userId, notifyTypeId, content);

            // 1. FCM 토큰 조회 (모바일용, 웹용 각각)
            List<UserFcm> userFcmList = userFcmDao.findUserFcmByUserId(userId);
            if (userFcmList.isEmpty()) {
                throw new IllegalStateException("FCM 토큰을 찾을 수 없습니다: userId=" + userId);
            }

            String messageId;
            if (notifyTypeId == 5) {
                messageId = generateMessageId(userId, notifyTypeId, scheduleStartDate, "o" + arg1 + "+" + arg2);
            } else {
                messageId = generateMessageId(userId, notifyTypeId, scheduleStartDate, content);
            }

            if (notificationDao.existsByMessageId(messageId)) {
                log.info("이미 처리된 메시지 ID입니다. 전송을 건너뜁니다. 메시지 ID: {}", messageId);
                return;  // 중복 메시지라면 전송하지 않음
            }

            System.out.println("메세지 id : " + messageId);


            // 3. 환경별 토큰 구분 후 전송
            for (UserFcm userFcm : userFcmList) {
                boolean tokenIsDev = userFcm.isDev();

                if (tokenIsDev != isDev) {
                    log.debug("⛔ 환경 불일치: message.isDev={}, token.isDev={}, fcmToken={}", isDev, tokenIsDev, userFcm.getFcmToken());
                    continue;
                }

                // 메시지의 환경 조건 부여
                NotificationDto.NotificationDtoBuilder builder = NotificationDto.builder()
                        .userId(userId)
                        .notifyTypeId(notifyTypeId)
                        .content(content)
                        .messageId(messageId)
                        .fcmToken(userFcm.getFcmToken())
                        .dev(isDev); // 이 토큰이 dev인지 여부를 메시지에 반영

                if (notifyTypeId == 5) {
                    builder.senderId((String) arg1).message((String) arg2);
                } else {
                    builder.senderId(null).message(null);
                }

                NotificationDto dto = builder.build();
                log.debug("📦 RabbitMQ 전송 전 DTO (환경 일치): {}", dto);
                NotificationMessageProducer.sendNotification(dto);
//
//            // 모바일과 웹에 대한 알림 전송
//            if (mobileFcmToken != null) {
//                builder.fcmToken(mobileFcmToken);  // 모바일 알림
//                NotificationDto mobileDto = builder.build();
//                log.debug("📦 모바일 RabbitMQ 전송 전 DTO: {}", mobileDto);
//                NotificationMessageProducer.sendNotification(mobileDto);
//            }
//
//            if (webFcmToken != null) {
//                builder.fcmToken(webFcmToken);  // 웹 알림
//                NotificationDto webDto = builder.build();
//                log.debug("📦 웹 RabbitMQ 전송 전 DTO: {}", webDto);
//                NotificationMessageProducer.sendNotification(webDto);
//            }

            }

            log.info("🚀 RabbitMQ 전송 완료");
            log.info(successLogFormat, arg1, arg2);
        } catch (Exception e) {
            log.error(errorLogMsg, e);
        }
    }
}
