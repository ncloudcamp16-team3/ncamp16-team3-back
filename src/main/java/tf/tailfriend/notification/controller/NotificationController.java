package tf.tailfriend.notification.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tf.tailfriend.global.response.CustomResponse;
import tf.tailfriend.notification.entity.dto.ChatNotificationDto;
import tf.tailfriend.notification.entity.dto.GetNotifyDto;
import tf.tailfriend.notification.service.NotificationService;

import java.util.List;
import java.util.Map;

import static tf.tailfriend.user.message.SuccessMessage.*;

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

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Integer id) {
        notificationService.markNotificationAsRead(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }


    @PostMapping("/chat")
    public ResponseEntity<?> sendChatNotification(@RequestBody ChatNotificationDto dto) {

        try {
            notificationService.handleChatNotification(dto);
            System.out.println("채팅 알림 처리 완료");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("채팅 알림 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("알림 처리 실패");
        }
    }


    @GetMapping("/check-notification")
    public ResponseEntity<?> checkNickname(@RequestParam Integer userId) {
        boolean exists = notificationService.existsByUserIdAndReadStatusFalse(userId);
        return ResponseEntity.ok(new CustomResponse(CHECK_NOTIFICATION_READ_SUCCESS.getMessage(), Map.of("exists", exists)));
    }
}
