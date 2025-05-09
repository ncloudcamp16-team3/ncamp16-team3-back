package tf.tailfriend.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tf.tailfriend.admin.dto.AnnounceResponseDto;
import tf.tailfriend.admin.entity.Announce;
import tf.tailfriend.admin.service.AnnounceService;
import tf.tailfriend.board.dto.AnnounceDto;
import tf.tailfriend.board.dto.BoardResponseDto;
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

    @GetMapping("/announce/list")
    public ResponseEntity<?> boardList(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false) Integer boardTypeId,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false, defaultValue = "all") String searchField
    ) {
        try {
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").descending());
            Page<AnnounceResponseDto> announces;
//            log.info("page: {}, size: {}, boardTypeId: {}, searchTerm: {}, searchField: {}", page, size, boardTypeId, searchTerm, searchField);

            // 검색어가 있는 경우 검색 로직 실행
            if (searchTerm != null && !searchTerm.isEmpty()) {
                announces = announceService.searchAnnounces(searchTerm, searchField, boardTypeId, pageRequest);
            }
            // 게시판 타입 필터링만 있는 경우
            else if (boardTypeId != null) {
                announces = announceService

                        .getAnnouncesByType(boardTypeId, pageRequest);
            }
            // 아무 조건 없는 경우 전체 조회
            else {
//                log.info("여기로 들어와야함");
                announces = announceService.getAllAnnounces(pageRequest);
            }

            return ResponseEntity.ok(announces);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "공지 목록 조회 실패: " + e.getMessage()));
        }
    }

    @GetMapping("/announce/{id}")
    public ResponseEntity<?> getBoardDetail(@PathVariable Integer id) {
        try {
            AnnounceDto announce = announceService.getAnnounceDetail(id);
            return ResponseEntity.ok(announce);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "공지 상세 조회 실패: " + e.getMessage()));
        }
    }

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
            Announce announce = announceService.createAnnounce(title, content, boardType, images);

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

    @DeleteMapping("/announce/{id}/delete")
    public ResponseEntity<?> deleteBoard(@PathVariable Integer id) {
        announceService.deleteAnnounceById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "게시글 삭제 완료"));
    }
}
