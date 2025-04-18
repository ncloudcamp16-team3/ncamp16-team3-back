package tf.tailfriend.petmeeting.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tf.tailfriend.global.response.CustomResponse;
import tf.tailfriend.petmeeting.service.PetmeetingService;

import static tf.tailfriend.petmeeting.message.SuccessMessage.GET_FRIENDS_SUCCESS;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class PetmeetingController {
    private final PetmeetingService petmeetingService;

    @GetMapping
    public ResponseEntity<?> getFriendList(
            @RequestParam String activityStatus,
            @RequestParam String dongName,
            @RequestParam String distance,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        return ResponseEntity.ok(
                new CustomResponse(GET_FRIENDS_SUCCESS.getMessage(), petmeetingService.getFriends())
        );
    }
}
