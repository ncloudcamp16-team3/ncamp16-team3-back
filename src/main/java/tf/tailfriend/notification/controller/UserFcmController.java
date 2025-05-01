package tf.tailfriend.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tf.tailfriend.notification.entity.UserFcm;
import tf.tailfriend.notification.entity.dto.UserFcmDto;
import tf.tailfriend.notification.service.UserFcmService;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserFcmController {

    private final UserFcmService userFcmService;

    @GetMapping("/exists")
    public ResponseEntity<?> checkFcmToken(@RequestParam Integer userId) {
        return userFcmService.findByUserId(userId)
                .map(userFcm -> {
                    String fcmToken = userFcm.getFcmToken();
                    if (fcmToken != null && !fcmToken.isEmpty()) {
                        return ResponseEntity.ok(fcmToken);
                    } else {
                        return ResponseEntity.noContent().build();
                    }
                })
                .orElse(ResponseEntity.noContent().build());
    }


    @PostMapping("/fcm")
    public ResponseEntity<?> saveOrUpdateFcmToken(@RequestBody UserFcmDto dto) {
        userFcmService.saveOrUpdate(dto);
        return ResponseEntity.ok().build();
    }
}