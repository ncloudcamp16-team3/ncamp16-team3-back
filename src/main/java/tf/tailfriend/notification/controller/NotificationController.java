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
}
