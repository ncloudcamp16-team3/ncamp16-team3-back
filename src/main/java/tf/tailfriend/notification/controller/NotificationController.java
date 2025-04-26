package tf.tailfriend.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tf.tailfriend.notification.entity.dto.NotificationDto;
import tf.tailfriend.notification.service.NotificationService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/notify")
    public ResponseEntity<?> notifyUser(@RequestBody NotificationDto notificationDto) {
        notificationService.sendNotification(notificationDto);
        return ResponseEntity.ok("알림 전송 완료");
    }
}
