package tf.tailfriend.notification.service;

import com.google.firebase.messaging.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tf.tailfriend.admin.entity.Announce;
import tf.tailfriend.admin.repository.AnnounceDao;
import tf.tailfriend.board.entity.Board;
import tf.tailfriend.board.entity.BoardType;
import tf.tailfriend.board.entity.Comment;
import tf.tailfriend.board.repository.BoardDao;
import tf.tailfriend.board.repository.CommentDao;
import tf.tailfriend.chat.entity.ChatRoom;
import tf.tailfriend.chat.repository.ChatRoomDao;
import tf.tailfriend.notification.config.NotificationMessageProducer;
import tf.tailfriend.notification.entity.UserFcm;
import tf.tailfriend.notification.entity.dto.ChatNotificationDto;
import tf.tailfriend.notification.entity.dto.GetNotifyDto;
import tf.tailfriend.notification.entity.dto.NotificationDto;
import tf.tailfriend.notification.entity.dto.UserFcmDto;
import tf.tailfriend.notification.repository.NotificationDao;
import tf.tailfriend.notification.repository.UserFcmDao;
import tf.tailfriend.notification.scheduler.NotificationScheduler;
import tf.tailfriend.petsta.entity.PetstaComment;
import tf.tailfriend.petsta.entity.PetstaPost;
import tf.tailfriend.petsta.entity.dto.PetstaCommentResponseDto;
import tf.tailfriend.petsta.repository.PetstaCommentDao;
import tf.tailfriend.petsta.repository.PetstaPostDao;
import tf.tailfriend.reserve.entity.Reserve;
import tf.tailfriend.reserve.repository.ReserveDao;
import tf.tailfriend.schedule.entity.Schedule;
import tf.tailfriend.schedule.repository.ScheduleDao;
import tf.tailfriend.user.entity.User;
import tf.tailfriend.user.repository.UserDao;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class NotificationService {

    private final UserFcmService userFcmService;
    private final CommentDao CommentDao;
    private final PetstaCommentDao PetstaCommentDao;
    private final ReserveDao reserveDao;
    private final ScheduleDao scheduleDao;
    private final ChatRoomDao chatRoomDao;
    private final AnnounceDao announceDao;
    private final UserFcmDao userFcmDao;
    private final NotificationScheduler notificationScheduler;
    private final PetstaPostDao petstaPostDao;
    private final BoardDao boardDao;
    private final NotificationDao notificationDao;
    private final CommentDao commentDao;
    private final PetstaCommentDao petstaCommentDao;
    private final UserDao userDao;


// 특정 사용자에게 직접 푸시 전송
@Transactional
public void sendNotificationToUser(NotificationDto dto) {

    boolean isLinux = System.getProperty("os.name").toLowerCase().contains("linux");
    boolean isDev = !isLinux; // 리눅스가 아니면 개발 환경

    List<UserFcm> userFcmList = userFcmDao.findAllByUserId(dto.getUserId());

    if (userFcmList.isEmpty()) {
        System.out.println("❌ FCM 토큰이 없는 사용자입니다: userId = " + dto.getUserId());
        return;
    }

    System.out.println("알림타입: "+dto.getNotifyTypeId());

    String title = "";
    String body = "";
    String image = "";
    String contentId = dto.getContent();
//    String imagePrefix = "https://kr.object.ncloudstorage.com/tailfriends-buck/uploads/notification";
    String imagePrefix = "https://kr.object.ncloudstorage.com/bitcamp-72";

    try {
        switch (dto.getNotifyTypeId()) {
            case 1 -> {
                Comment comment = CommentDao.findById(Integer.valueOf(contentId))
                        .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다"));
                title = "게시글에 댓글이 달렸습니다.";
                body = comment.getContent();
                image = imagePrefix + "/comment.png";
            }
            case 2 -> {
                PetstaComment petstaComment = PetstaCommentDao.findById(Integer.valueOf(contentId))
                        .orElseThrow(() -> new RuntimeException("펫스타 댓글을 찾을 수 없습니다"));
                title = "펫스타에 댓글이 달렸습니다.";
                body = petstaComment.getContent();
                image = imagePrefix + "/petsta.png";
            }
            case 3 -> {
                Reserve reserve = reserveDao.findById(Integer.valueOf(contentId))
                        .orElseThrow(() -> new RuntimeException("예약 내역을 찾을 수 없습니다"));
                title = "오늘은 " + reserve.getFacility().getName() + " 예약이 있습니다.";
                body = "예약 시간: " + reserve.getEntryTime();
                image = imagePrefix + "/reserve.png";
            }
            case 4 -> {
                Schedule schedule = scheduleDao.findById(Integer.valueOf(contentId))
                        .orElseThrow(() -> new RuntimeException("일정을 찾을 수 없습니다"));
                title = "오늘은 " + schedule.getTitle() + " 일정이 있습니다.";
                body = "일정 시작: " + schedule.getStartDate();
                image = imagePrefix + "/schedule.png";
            }
            case 5 -> {
                User user = userDao.findById(Integer.valueOf(dto.getSenderId()))
                        .orElseThrow(() -> new RuntimeException("채팅 보낸 유저를 찾을 수 없습니다."));
                title = user.getNickname() + " 님으로부터 메시지가 도착했습니다.";
                body = dto.getMessage();
                image = imagePrefix + "/chat.png";
            }
            case 6 -> {
                Announce announce = announceDao.findById(Integer.valueOf(contentId))
                        .orElseThrow(() -> new RuntimeException("공지글을 찾을 수 없습니다"));
                title = "새로운 공지가 등록되었습니다.";
                body = announce.getTitle();
                image = imagePrefix + "/global.png";
            }
            case 7 -> {
                title = "채팅방에 구독 되었습니다.";
                body = "내 채팅방 목록을 갱신합니다.";
                image = "";
            }
            default -> {
                title = "알림";
                body = "새로운 알림이 도착했습니다.";
                image = imagePrefix + "/default.png";
            }
        }

        for (UserFcm userFcm : userFcmList) {
            // 현재 환경과 토큰의 isDev 일치 여부 확인
            if (userFcm.isDev() != isDev) {
                System.out.println("🚫 환경 불일치로 푸시 제외: token=" + userFcm.getFcmToken());
                continue;
            }
            String fcmToken = userFcm.getFcmToken();

            Message message;
            if (dto.getNotifyTypeId() == 7) {
                message = Message.builder()
                        .setToken(fcmToken)
                        .putData("type", "FETCH_ROOMS") // ✅ 중요: 프론트 구분용
                        .putData("title", title)
                        .putData("body", body)
                        .build();
            }
            else {

            message = Message.builder()
                    .setToken(fcmToken)
                    .putData("title", title)
                    .putData("body", body)
                    .putData("icon", image)
                    .build();
            }

            try {
                FirebaseMessaging.getInstance().send(message);
                System.out.println("✅ 푸시 전송 성공: userId = " + dto.getUserId() + ", token = " + fcmToken);
            } catch (FirebaseMessagingException e) {
                if ("UNREGISTERED".equals(e.getMessagingErrorCode().name())) {
                    System.err.println("❌ 유효하지 않은 FCM 토큰. 삭제 처리: " + fcmToken);
                    userFcmDao.delete(userFcm);
                } else {
                    System.err.println("❌ 푸시 전송 실패 (Firebase): " + e.getMessage());
                }
            }
        }
    } catch (Exception e) {
        System.err.println("❌ 알림 처리 중 예외 발생: " + e.getMessage());
        e.printStackTrace();
    }
}
public boolean existsByUserIdAndReadStatusFalse(Integer userId) {
    return notificationDao.existsByUserIdAndReadStatusFalseAndNotificationTypeIdNot(userId, 7);
}


    public void sendAnnounceNotificationToAllUsers(Announce announce) {
        boolean isLinux = System.getProperty("os.name").toLowerCase().contains("linux");
        boolean isDev = !isLinux; // 리눅스가 아니면 개발 환경

        List<UserFcm> userFcmtokens = userFcmDao.findAll();

        for (UserFcm userFcm : userFcmtokens) {
            Integer userId = userFcm.getUserId();
            String formattedCreatedAt = announce.getCreatedAt()
                    .atZone(ZoneId.of("Asia/Seoul"))
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            notificationScheduler.sendNotificationAndSaveLog(

                    userId,
                    6,
                    String.valueOf(announce.getId()),
                    formattedCreatedAt,
                    "📌 공지 알림 전송 완료: 제목={}, 내용={}",
                    announce.getTitle(),
                    announce.getContent(),
                    "❌ 공지 알림 전송 실패: announceId=" + announce.getId(),
                    isDev
            );
        }
    }


    public void sendChatroomforOtherUser(Integer userId2) {
        boolean isLinux = System.getProperty("os.name").toLowerCase().contains("linux");
        boolean isDev = !isLinux; // 리눅스가 아니면 개발 환경

        // 시작 로그
        System.out.println("채팅방 알림 전송 시작: userId2=" + userId2 + ", isDev=" + isDev);

        List<UserFcm> userFcmtokens = userFcmDao.findUserFcmByUserId(userId2);

        if (userFcmtokens == null || userFcmtokens.isEmpty()) {
            System.out.println("알림을 보낼 FCM 토큰이 없습니다. userId2=" + userId2);
        } else {
            System.out.println("FCM 토큰 수: " + userFcmtokens.size() + "개");
        }

        for (UserFcm userFcm : userFcmtokens) {
            Integer userId = userFcm.getUserId();
            String formattedCreatedAt = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

            // 디버깅을 위한 로그: 유저 아이디와 시간 확인
            System.out.println("FCM 토큰 전송 대상: userId=" + userId + ", 시간=" + formattedCreatedAt);

            try {
                // 알림 전송 및 로그 기록
                notificationScheduler.sendNotificationAndSaveLog(
                        userId,
                        7, // 알림 유형 ID
                        "", // 제목 (빈 값, 필요한 정보 추가 가능)
                        formattedCreatedAt, // 생성 시간
                        "📌 채팅방 생성 알림 전송 완료: 제목={}, 내용={}", // 성공 메시지
                        "", // 메시지
                        "", // 메시지
                        "❌ 채팅방 생성 알림 전송 실패", // 실패 메시지
                        isDev
                );
                // 성공 로그
                System.out.println("채팅방 생성 알림 전송 완료: userId=" + userId);
            } catch (Exception e) {
                // 오류 로그
                System.out.println("채팅방 생성 알림 전송 중 오류 발생: userId=" + userId + ", 오류=" + e.getMessage());
                e.printStackTrace();  // 예외의 전체 스택 트레이스 출력
            }
        }

        // 종료 로그
        System.out.println("채팅방 알림 전송 완료: userId2=" + userId2);
    }



    public void sendPetstaCommentNotification(PetstaCommentResponseDto dto, Integer postId) {

        boolean isLinux = System.getProperty("os.name").toLowerCase().contains("linux");
        boolean isDev = !isLinux; // 리눅스가 아니면 개발 환경

        // 게시글 정보 조회
        PetstaPost petstaPost = petstaPostDao.getPetstaPostById(postId);
        Integer postOwnerId = petstaPost.getUser().getId();
        Integer commentWriterId = dto.getUserId();

        Set<Integer> targetUserIds = new HashSet<>();

        if (!postOwnerId.equals(commentWriterId)) {
            targetUserIds.add(postOwnerId);
        }
        if (dto.getParentId() != null) {
            // 1. 부모 댓글 조회
            PetstaComment parentComment = petstaCommentDao.findById(dto.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent comment not found"));
            Integer parentCommentWriterId = parentComment.getUser().getId();

            // 2-1. 부모 댓글 작성자에게 알림 (자신이 아니면)
            if (!parentCommentWriterId.equals(commentWriterId)) {
                targetUserIds.add(parentCommentWriterId);
            }

            // 2-2. 형제 대댓글 작성자들에게 알림
            List<PetstaComment> siblingReplies = petstaCommentDao.findRepliesByParentId(dto.getParentId());
            for (PetstaComment sibling : siblingReplies) {
                Integer siblingWriterId = sibling.getUser().getId();
                if (!siblingWriterId.equals(commentWriterId)) {
                    targetUserIds.add(siblingWriterId);
                }
            }
        }

        System.out.println("✅ 펫스타 댓글 알림 대상 유저 ID 목록: " + targetUserIds);

        // 알림 전송
        for (Integer userId : targetUserIds) {

            notificationScheduler.sendNotificationAndSaveLog(
                    userId,
                    2, // 댓글 알림 타입
                    String.valueOf(dto.getId()),
                    dto.getCreatedAt(),
                    "💬 펫스타 댓글 알림 전송 완료: 작성 유저 닉네임={}, 댓글내용={}",
                    dto.getUserName(),
                    dto.getContent(),
                    "❌ 펫스타 댓글 알림 전송 실패: commentId=" + dto.getId(),
                    isDev
            );
        }
    }

    public void sendBoardCommentNotification(Comment comment) {
        boolean isLinux = System.getProperty("os.name").toLowerCase().contains("linux");
        boolean isDev = !isLinux; // 리눅스가 아니면 개발 환경

        // 게시글 정보 조회
        Board board = boardDao.getBoardById(comment.getBoard().getId()); // 댓글의 보드id 받음
        Integer postOwnerId = board.getUser().getId();
        Integer commentWriterId = comment.getUser().getId();


        // 부모 댓글 작성자 ID 추출

        Set<Integer> targetUserIds = new HashSet<>();

        if (!postOwnerId.equals(commentWriterId)) {
            targetUserIds.add(postOwnerId);
        }

        if (comment.getParent() != null) {
            Integer parentCommentWriterId = comment.getParent().getUser().getId();

            // 2-1. 부모 댓글 작성자에게 알림 (자신이 아니면)
            if (!parentCommentWriterId.equals(commentWriterId)) {
                targetUserIds.add(parentCommentWriterId);
            }

            // 2-2. 형제 대댓글 작성자들에게 알림
            List<Comment> siblingReplies = commentDao.findRepliesByParentId(comment.getParent().getId());
            for (Comment sibling : siblingReplies) {
                Integer siblingWriterId = sibling.getUser().getId();
                if (!siblingWriterId.equals(commentWriterId)) {
                    targetUserIds.add(siblingWriterId);
                }
            }
        }


        System.out.println("✅ 게시판 댓글 알림 대상 유저 ID 목록: "+ targetUserIds);

        // 알림 전송
        for (Integer userId : targetUserIds) {
            String formattedCreatedAt = comment.getCreatedAt()
                    .atZone(ZoneId.of("Asia/Seoul"))
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            notificationScheduler.sendNotificationAndSaveLog(
                    userId,
                    1, // 게시판 댓글 알림 타입
                    String.valueOf(comment.getId()), // 댓글 id
                    formattedCreatedAt,
                    "💬 댓글 알림 전송 완료: 게시글 제목={}, 댓글={}",
                    comment.getBoard().getTitle(),
                    comment.getContent(),
                    "❌ 댓글 알림 전송 실패: commentId=" + comment.getId()
                    ,isDev
            );
        }
    }

    public void handleChatNotification(ChatNotificationDto dto) {
        boolean isLinux = System.getProperty("os.name").toLowerCase().contains("linux");
        boolean isDev = !isLinux; // 리눅스가 아니면 개발 환경

        try {

            String formattedCreatedAt = dto.getCreatedAt()
                    .atZone(ZoneId.of("Asia/Seoul"))
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            notificationScheduler.sendNotificationAndSaveLog(
                    dto.getUserId(),
                    5,
                    dto.getChannelId(),
                    formattedCreatedAt,
                    "💬 채팅 알림 전송 완료: 보낸사람 id={}, 메시지={}",
                    dto.getSenderId(),
                    dto.getMessage(),
                    "❌ 채팅 알림 전송 실패: channelId=" + dto.getChannelId(),
                    isDev
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public GetNotifyDto createNotifyDto(tf.tailfriend.notification.entity.Notification notification) {

        String content;

        int typeId = notification.getNotificationType().getId();
        if (typeId == 1) {
            Integer commentId = Integer.valueOf(notification.getContent());
            content = commentDao.findById(commentId)
                    .map(comment -> comment.getBoard().getId().toString())
                    .orElse("UNKNOWN");
        } else if (typeId == 2) {
            Integer petstaCommentId = Integer.valueOf(notification.getContent());
            content = petstaCommentDao.findById(petstaCommentId)
                    .map(c -> c.getPost().getId().toString())
                    .orElse("UNKNOWN");
        } else {
            content = notification.getContent();
        }

        // ✅ KST 시간 포맷팅
        String formattedCreatedAt = notification.getCreatedAt()
                .atZone(ZoneId.of("Asia/Seoul"))
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        return GetNotifyDto.builder()
                .id(notification.getId())
                .userId(notification.getUser().getId())
                .notificationTypeId(typeId)
                .readStatus(notification.getReadStatus())
                .createdAt(formattedCreatedAt) // ✅ 적용된 포맷
                .content(content)
                .build();
    }



    public List<GetNotifyDto> getNotificationsByUserId(Integer userId) {
        List<tf.tailfriend.notification.entity.Notification> notifications = notificationDao.findByUserIdOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .filter(notification -> notification.getNotificationType().getId() != 7) // ← type 7 제외
                .map(this::getNotificationDetails)
                .collect(Collectors.toList());
    }



    public GetNotifyDto getNotificationDetails(tf.tailfriend.notification.entity.Notification notification) {

        GetNotifyDto dto = createNotifyDto(notification);

        try {
            switch (notification.getNotificationType().getId()) {
                case 1 -> {
                    try {
                        Comment comment = commentDao.findById(Integer.valueOf(notification.getContent())) // ← 여기서 원본 댓글 ID 사용
                                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다"));

                        dto.setTitle("게시글에 댓글이 달렸습니다.");
                        dto.setBody(comment.getContent());
                    } catch (RuntimeException e) {
                        dto.setTitle("댓글을 찾을 수 없습니다.");
                        dto.setBody("관련 댓글을 확인할 수 없습니다.");
                    }
                }
                case 2 -> {
                    try {
                        PetstaComment petstaComment = petstaCommentDao.findById(Integer.valueOf(notification.getContent())) // ← 원본 댓글 ID 사용
                                .orElseThrow(() -> new RuntimeException("펫스타 댓글을 찾을 수 없습니다"));
                        System.out.println("조회할 댓글 아이디 :"+notification.getContent());
                        dto.setTitle("펫스타에 댓글이 달렸습니다.");
                        dto.setBody(petstaComment.getContent());
                    } catch (RuntimeException e) {
                        dto.setTitle("펫스타 댓글을 찾을 수 없습니다.");
                        dto.setBody("관련 펫스타 댓글을 확인할 수 없습니다.");
                    }
                }
                case 3 -> {
                    try {
                        Reserve reserve = reserveDao.findById(Integer.valueOf(notification.getContent()))
                                .orElseThrow(() -> new RuntimeException("예약 내역을 찾을 수 없습니다"));
                        dto.setTitle("오늘은 " + reserve.getFacility().getName() + " 예약이 있습니다.");
                        dto.setBody("예약 시간: " + reserve.getEntryTime());
                    } catch (RuntimeException e) {
                        dto.setTitle("예약 내역을 찾을 수 없습니다.");
                        dto.setBody("관련 예약을 확인할 수 없습니다.");
                    }
                }
                case 4 -> {
                    try {
                        Schedule schedule = scheduleDao.findById(Integer.valueOf(notification.getContent()))
                                .orElseThrow(() -> new RuntimeException("일정을 찾을 수 없습니다"));
                        dto.setTitle("오늘은 " + schedule.getTitle() + " 일정이 있습니다.");
                        dto.setBody("일정 시작: " + schedule.getStartDate());
                    } catch (RuntimeException e) {
                        dto.setTitle("일정을 찾을 수 없습니다.");
                        dto.setBody("관련 일정을 확인할 수 없습니다.");
                    }
                }
                case 5 -> {
                    try {
                        dto.setTitle("");
                        dto.setBody("");
                    } catch (RuntimeException e) {
                        dto.setTitle("보낸 사람 정보를 확인할 수 없습니다.");
                        dto.setBody("메세지를 확인할 수 없습니다.");
                    }
                }
                case 6 -> {
                    try {
                        Announce announce = announceDao.findById(Integer.valueOf(notification.getContent()))
                                .orElseThrow(() -> new RuntimeException("공지글을 찾을 수 없습니다"));
                        dto.setTitle("새로운 공지가 등록되었습니다.");
                        dto.setBody(announce.getTitle());
                    } catch (RuntimeException e) {
                        dto.setTitle("공지글을 찾을 수 없습니다.");
                        dto.setBody("관련 공지글을 확인할 수 없습니다.");
                    }
                }
                case 7 -> {
                    try {
                        dto.setTitle("채팅방에 구독 되었습니다.");
                        dto.setBody("내 채팅방 목록을 갱신합니다.");
                    } catch (RuntimeException e) {
                        dto.setTitle("채팅방");
                        dto.setBody("관련 공지글을 확인할 수 없습니다.");
                    }
                }

                default -> {
                    dto.setTitle("알림 내용이 없습니다.");
                    dto.setBody("알림 내용을 확인하세요.");
                }
            }

        } catch (NumberFormatException e) {
            dto.setTitle("알림 ID가 올바르지 않습니다.");
            dto.setBody("내용을 확인할 수 없습니다.");
        }

        return dto;
    }

    @Transactional
    public void deleteNotificationById(Integer notificationId) {
        notificationDao.deleteById(notificationId);
    }

    @Transactional
    public void deleteAllNotificationsByUserId(Integer userId) {
        notificationDao.deleteByUserId(userId);
    }

    @Transactional
    public void markNotificationAsRead(Integer id) {
        tf.tailfriend.notification.entity.Notification notification = notificationDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));

        notification.markAsRead(); // 변경
    }




}