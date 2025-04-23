package tf.tailfriend.petmeeting.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tf.tailfriend.global.response.CustomResponse;
import tf.tailfriend.petmeeting.dto.FindFriendRequestDTO;
import tf.tailfriend.petmeeting.dto.PetFriendDTO;
import tf.tailfriend.petmeeting.service.PetmeetingService;

import static tf.tailfriend.petmeeting.message.SuccessMessage.GET_FRIENDS_SUCCESS;

@RestController
@RequestMapping("/api/petmeeting")
@RequiredArgsConstructor
@Slf4j
public class PetmeetingController {
    private final PetmeetingService petmeetingService;

    @PostMapping("/friends")
    public ResponseEntity<?> getFriendList(@RequestBody FindFriendRequestDTO findFriendRequestDTO) {

        log.info("\n\n\n\n\n\n\n\nfindFriendRequestDTO : " + findFriendRequestDTO);

        Page<PetFriendDTO> petFriends =  petmeetingService.getFriends(
                findFriendRequestDTO.getActivityStatus(),
                findFriendRequestDTO.getDongName(),
                findFriendRequestDTO.getDistance(),
                findFriendRequestDTO.getPage(),
                findFriendRequestDTO.getSize(),
                findFriendRequestDTO.getLatitude(),
                findFriendRequestDTO.getLongitude()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new CustomResponse(GET_FRIENDS_SUCCESS.getMessage(), petFriends));
    }
}
