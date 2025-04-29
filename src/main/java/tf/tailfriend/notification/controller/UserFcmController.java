package tf.tailfriend.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tf.tailfriend.notification.entity.dto.UserFcmDto;
import tf.tailfriend.notification.service.UserFcmService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserFcmController {

    private final UserFcmService userFcmService;

    @GetMapping("/exists")
    public ResponseEntity<?> checkFcmExists(@RequestParam Integer userId) {
        boolean exists = userFcmService.existsByUserId(userId);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @PostMapping("/fcm")
    public ResponseEntity<Void> registerOrUpdateFcmToken(@RequestBody UserFcmDto dto) {
        userFcmService.registerOrUpdateFcmToken(dto);
        return ResponseEntity.ok().build();
    }
}