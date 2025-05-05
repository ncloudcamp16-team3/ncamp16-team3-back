package tf.tailfriend.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
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
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    public void sendNotificationToUser(NotificationDto dto) {
        List<UserFcm> userFcmList =  userFcmDao.findAllByUserId(dto.getUserId());

        if (userFcmList.isEmpty()) {
            System.out.println("❌ FCM 토큰이 없는 사용자입니다: userId = " + dto.getUserId());
            return;
        }

        String title = "";
        String body = "";
        String image = "";
        String contentId = dto.getContent();
        String imagePrefix = "https://kr.object.ncloudstorage.com/tailfriends-buck/uploads/notification";

        try {
            switch (dto.getNotifyTypeId()) {
                case 1 -> {
                    Comment comment = CommentDao.findById(Integer.valueOf(contentId))
                            .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다"));
                    title = "내 게시글에 댓글이 달렸습니다.";
                    body = comment.getContent();
                    image = imagePrefix + "/comment.png";
                }
                case 2 -> {
                    PetstaComment petstaComment = PetstaCommentDao.findById(Integer.valueOf(contentId))
                            .orElseThrow(() -> new RuntimeException("펫스타 댓글을 찾을 수 없습니다"));
                    title = "내 펫스타에 댓글이 달렸습니다.";
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
                default -> {
                    title = "알림";
                    body = "새로운 알림이 도착했습니다.";
                    image = imagePrefix + "/default.png";
                }
            }

            for (UserFcm userFcm : userFcmList) {
                String fcmToken = userFcm.getFcmToken();

                Message message = Message.builder()
                        .setToken(fcmToken)
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .setImage(image)
                                .build())
                        .putData("icon", image)
                        .build();

                FirebaseMessaging.getInstance().send(message);
                System.out.println("✅ 푸시 전송 성공: userId = " + dto.getUserId() + ", token = " + fcmToken);
            }

        } catch (Exception e) {
            System.err.println("❌ 푸시 전송 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendAnnounceNotificationToAllUsers(Announce announce) {
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
                    "❌ 공지 알림 전송 실패: announceId=" + announce.getId()
            );
        }
    }

    public void sendPetstaCommentNotification(PetstaCommentResponseDto dto, Integer postId) {
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
                    "❌ 펫스타 댓글 알림 전송 실패: commentId=" + dto.getId()
            );
        }
    }

    public void sendBoardCommentNotification(Comment comment) {
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
            );
        }
    }

    public void handleChatNotification(ChatNotificationDto dto) {

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
                    "❌ 채팅 알림 전송 실패: channelId=" + dto.getChannelId()
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
                .map(this::getNotificationDetails) // title/body 설정 포함
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

                        dto.setTitle("내 게시글에 댓글이 달렸습니다.");
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
                        dto.setTitle("내 펫스타에 댓글이 달렸습니다.");
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