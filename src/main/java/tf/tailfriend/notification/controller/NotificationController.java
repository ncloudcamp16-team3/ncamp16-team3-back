package tf.tailfriend.notification.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
//import tf.tailfriend.notification.config.PushNotificationService;
import tf.tailfriend.notification.entity.dto.NotificationDto;
import tf.tailfriend.notification.service.NotificationService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
public class NotificationController {

//    private final NotificationService notificationService;
//
//
//    @PostMapping("/push")
//    public ResponseEntity<Void> sendNotification(@RequestBody NotificationDto notificationDto) {
//        notificationService.sendNotification(notificationDto);
//        return ResponseEntity.ok().build(); // 성공적으로 알림 전송
//    }

}
