package tf.tailfriend.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tf.tailfriend.admin.dto.AnnounceResponseDto;
import tf.tailfriend.admin.entity.Announce;
import tf.tailfriend.admin.service.AnnounceService;
import tf.tailfriend.board.entity.BoardType;
import tf.tailfriend.board.service.BoardTypeService;
import tf.tailfriend.file.entity.File;
import tf.tailfriend.file.service.FileService;
import tf.tailfriend.notification.entity.UserFcm;
import tf.tailfriend.notification.repository.UserFcmDao;
import tf.tailfriend.notification.scheduler.NotificationScheduler;
import tf.tailfriend.notification.service.NotificationService;
import tf.tailfriend.user.entity.User;
import tf.tailfriend.user.repository.UserDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminAnnounceController {

    private final BoardTypeService boardTypeService;
    private final AnnounceService announceService;
    private final NotificationService notificationService;

    @PostMapping("/announce/post")
    public ResponseEntity<?> createAnnounce(
            @RequestParam("boardTypeId") Integer boardTypeId,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        try {
            log.info("/announce/post, boardTypeId: {}", boardTypeId);
            BoardType boardType = boardTypeService.getBoardTypeById(boardTypeId);
            if (boardType == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "유효하지 않은 게시판 타입입니다"));
            }

            // 알람 전송을 위한 객체 저장
            Announce announce=announceService.createAnnounce(title, content, boardType, images);

            try {
                notificationService.sendAnnounceNotificationToAllUsers(announce);
            } catch (Exception e) {
                log.warn("공지 알림 전송 중 예외 발생: {}", e.getMessage());
            }

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "공지사항이 성공적으로 등록되었습니다"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "공지사항 등록 실패: " + e.getMessage()));
        }
    }
}
