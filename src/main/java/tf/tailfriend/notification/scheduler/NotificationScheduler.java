package tf.tailfriend.notification.scheduler;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tf.tailfriend.notification.service.NotificationService;
import tf.tailfriend.schedule.entity.Schedule;
import tf.tailfriend.schedule.repository.ScheduleDao;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final ScheduleDao scheduleDao;
    private final NotificationService notificationService;

    @PostConstruct
    public void init() {
        System.out.println("🔔 NotificationScheduler 초기화됨");
    }


    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void sendScheduledNotifications() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tenMinutesLater = now.plusMinutes(10);

        // 로그 추가: 스케줄 작업이 실행되었는지 확인
        log.debug("🔄 NotificationScheduler 실행됨: 현재 시간 = {}, 10분 후 = {}", now, tenMinutesLater);

        // 현재 시간 기준, 10분 뒤 시작 예정인 일정 조회
        List<Schedule> upcomingSchedules = scheduleDao.findByStartDateBetween(now, tenMinutesLater);

        // 일정 조회 결과 로그 추가
        if (upcomingSchedules.isEmpty()) {
            log.debug("📅 10분 후 시작 예정인 일정 없음.");
        } else {
            log.debug("📅 10분 후 시작 예정인 일정 개수: {}", upcomingSchedules.size());
        }

        for (Schedule schedule : upcomingSchedules) {
            try {
                notificationService.sendNotificationToUser(
                        schedule.getUser().getId(),
                        "일정 시작 10분 전!",
                        schedule.getTitle() + " 일정이 곧 시작합니다."
                );
                log.info("🔔 알림 전송 완료: userId={}, title={}", schedule.getUser().getId(), schedule.getTitle());
            } catch (Exception e) {
                log.error("❌ 알림 전송 실패: scheduleId={}", schedule.getId(), e);
            }
        }
    }
}