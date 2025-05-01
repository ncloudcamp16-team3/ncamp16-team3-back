package tf.tailfriend.notification.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
//import tf.tailfriend.notification.config.PushNotificationService;
import tf.tailfriend.notification.entity.dto.GetNotifyDto;
import tf.tailfriend.notification.entity.dto.NotificationDto;
import tf.tailfriend.notification.service.NotificationService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notification")
public class NotificationController {

private final NotificationService notificationService;


    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GetNotifyDto>> getUserNotifications(@PathVariable Integer userId) {
        List<GetNotifyDto> notifyList = notificationService.getNotificationsByUserId(userId);
        return ResponseEntity.ok(notifyList);
    }

    // 프라이머리 키 기반 알림 삭제
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Integer notificationId) {
        notificationService.deleteNotificationById(notificationId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    // 특정 유저의 모든 알림 삭제
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteAllNotifications(@PathVariable Integer userId) {
        notificationService.deleteAllNotificationsByUserId(userId);
        return ResponseEntity.noContent().build();
    }
}
