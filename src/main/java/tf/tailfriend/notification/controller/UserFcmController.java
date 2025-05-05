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



    @PostMapping("/fcm")
    public ResponseEntity<?> saveOrUpdateFcmToken(@RequestBody UserFcmDto dto) {
        userFcmService.saveOrUpdate(dto);
        System.out.println(dto.getFcmToken());
        System.out.println(dto.getUserId());
        System.out.println(dto.isDev());
        System.out.println(dto.isMobile());
        return ResponseEntity.ok().build();
    }
}
