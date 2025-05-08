package tf.tailfriend.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tf.tailfriend.chat.entity.dto.ChatRoomListResponseDto;
import tf.tailfriend.chat.service.ChatService;
import tf.tailfriend.global.config.UserPrincipal;
import tf.tailfriend.global.response.CustomResponse;
import tf.tailfriend.notification.service.NotificationService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final NotificationService notificationService;

    @PostMapping("/room")
    public ResponseEntity<String> createRoom(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam("userId2") Integer userId2
    ) {
        Integer userId1 = userPrincipal.getUserId();
        String uniqueId = chatService.createOrGetRoom(userId1,userId2);
        notificationService.sendChatroomforOtherUser(userId2);

        return ResponseEntity.ok(uniqueId);
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomListResponseDto>> getMyChatRooms(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Integer userId = userPrincipal.getUserId();
        List<ChatRoomListResponseDto> rooms = chatService.findAllMyChatRooms(userId);
        System.out.println(rooms);
        return ResponseEntity.ok(rooms);
    }



    @PostMapping("/match/start")
    public void match(@RequestParam Integer petId1, @RequestParam Integer petId2) {
        chatService.checkOrCreateMatch(petId1, petId2);
    }

    @GetMapping("/match/check")
    public CustomResponse checkMatch(@RequestParam Integer petId1, @RequestParam Integer petId2) {
        boolean matched = chatService.isMatched(petId1, petId2);
        return new CustomResponse("매칭 여부 조회 성공", matched);
    }

    @PostMapping("/trade/start")
    public void tradeMatchStart(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam Integer postId) {
        Integer userId = userPrincipal.getUserId();
        chatService.checkOrCreateTradeMatch(userId, postId);
    }

    @GetMapping("/trade/check")
    public CustomResponse checkTradeMatch(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam Integer postId) {
        Integer userId = userPrincipal.getUserId();
        boolean matched = chatService.isTradeMatched(userId, postId);
        return new CustomResponse("거래 매칭 여부 조회 성공", matched);
    }


}


