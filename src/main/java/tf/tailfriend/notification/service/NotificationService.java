package tf.tailfriend.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tf.tailfriend.admin.entity.Announce;
import tf.tailfriend.admin.repository.AnnounceDao;
import tf.tailfriend.board.entity.Comment;
import tf.tailfriend.board.repository.CommentDao;
import tf.tailfriend.chat.entity.ChatRoom;
import tf.tailfriend.chat.repository.ChatRoomDao;
import tf.tailfriend.notification.config.NotificationMessageProducer;
import tf.tailfriend.notification.entity.UserFcm;
import tf.tailfriend.notification.entity.dto.NotificationDto;
import tf.tailfriend.notification.entity.dto.UserFcmDto;
import tf.tailfriend.petsta.entity.PetstaComment;
import tf.tailfriend.petsta.repository.PetstaCommentDao;
import tf.tailfriend.reserve.entity.Reserve;
import tf.tailfriend.reserve.repository.ReserveDao;
import tf.tailfriend.schedule.entity.Schedule;
import tf.tailfriend.schedule.repository.ScheduleDao;

import java.util.Optional;


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



    // 특정 사용자에게 직접 푸시 전송
    public void sendNotificationToUser(NotificationDto dto) {
        userFcmService.findByUserId(dto.getUserId()).ifPresentOrElse(
                userFcm -> {
                    String fcmToken = userFcm.getFcmToken();
                    String title = "";
                    String body = "";

                    try {
                        int contentId = Integer.parseInt(dto.getContent());

                        switch (dto.getNotifyTypeId()) {
                            case 1 -> {
                                // 일반 댓글
                                Comment comment = CommentDao.findById(contentId)
                                        .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다"));
                                title = "내 게시글에 댓글이 달렸습니다.";
                                body = comment.getContent();
                            }
                            case 2 -> {
                                // 펫스타 댓글
                                PetstaComment petstaComment = PetstaCommentDao.findById(contentId)
                                        .orElseThrow(() -> new RuntimeException("펫스타 댓글을 찾을 수 없습니다"));
                                title = "내 펫스타에 댓글이 달렸습니다.";
                                body = petstaComment.getContent();
                            }
                            case 3 -> {
                                // 예약 알림
                                Reserve reserve = reserveDao.findById(contentId)
                                        .orElseThrow(() -> new RuntimeException("예약 내역을 찾을 수 없습니다"));
                                title = "오늘은 " + reserve.getFacility().getName() + " 예약이 있습니다.";
                                body = "예약 내용을 확인해보세요.";
                            }
                            case 4 -> {
                                // 일정 알림
                                Schedule schedule = scheduleDao.findById(contentId)
                                        .orElseThrow(() -> new RuntimeException("일정을 찾을 수 없습니다"));
                                title = "오늘은 " + schedule.getTitle() + " 일정이 있습니다.";
                                body = "일정 시작: " + schedule.getStartDate();
                            }
                            case 5 -> {
                                // 채팅 알림
                                title = "새로운 메세지가 왔습니다.";
                                body = "채팅 내용을 확인하세요.";
                            }
                            case 6 -> {
                                // 공지 알림
                                Announce announce = announceDao.findById(contentId)
                                        .orElseThrow(() -> new RuntimeException("공지글을 찾을 수 없습니다"));
                                title = "새로운 공지가 등록되었습니다.";
                                body = announce.getTitle();
                            }
                            default -> {
                                title = "알림";
                                body = "새로운 알림이 도착했습니다.";
                            }
                        }

                        Message message = Message.builder()
                                .setToken(fcmToken)
                                .setNotification(Notification.builder()
                                        .setTitle(title)
                                        .setBody(body)
                                        .build())
                                .build();

                        FirebaseMessaging.getInstance().send(message);
                        System.out.println("푸시 전송 성공: " + dto.getUserId());

                    } catch (Exception e) {
                        System.err.println("푸시 전송 실패: " + e.getMessage());
                        e.printStackTrace();
                    }
                },
                () -> {
                    System.out.println("FCM 토큰이 없는 사용자입니다: userId = " + dto.getUserId());
                }
        );
    }
}