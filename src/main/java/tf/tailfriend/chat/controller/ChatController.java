package tf.tailfriend.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tf.tailfriend.chat.entity.ChatRoom;
import tf.tailfriend.chat.entity.dto.ChatRoomResponseDto;
import tf.tailfriend.chat.service.ChatService;
import tf.tailfriend.global.config.UserPrincipal;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/room")
    public ResponseEntity<String> createRoom(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam("userId2") Integer userId2
    ) {
        Integer userId1 = userPrincipal.getUserId();
        String uniqueId = chatService.createOrGetRoom(userId1,userId2);

        return ResponseEntity.ok(uniqueId);
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<String>> getMyChatRooms(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Integer userId = userPrincipal.getUserId();
        List<String> rooms = chatService.findAllMyChatRoomIds(userId);
        return ResponseEntity.ok(rooms); // ✅ 바로 uniqueId 리스트 반환
    }


}


