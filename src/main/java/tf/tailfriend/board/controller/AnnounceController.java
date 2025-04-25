package tf.tailfriend.board.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tf.tailfriend.admin.service.AnnounceService;
import tf.tailfriend.board.dto.AnnounceDto;
import tf.tailfriend.board.exception.GetAnnounceException;
import tf.tailfriend.global.response.CustomResponse;

import java.util.List;

import static tf.tailfriend.board.message.SuccessMessage.GET_ANNOUNCE_DETAIL_SUCCESS;
import static tf.tailfriend.board.message.SuccessMessage.GET_ANNOUNCE_SUCCESS;

@RestController
@RequestMapping("/api/announce")
@RequiredArgsConstructor
@Slf4j
public class AnnounceController {

    private final AnnounceService announceService;

    @GetMapping("/{boardTypeId}")
    public ResponseEntity<?> getAnnounces(@PathVariable Integer boardTypeId) {
        try {
            log.info("\n\n 요청 게시판 타입ID {}", boardTypeId);
            List<AnnounceDto> announceDtos = announceService.getAnnounces(boardTypeId);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new CustomResponse(GET_ANNOUNCE_SUCCESS.getMessage(), announceDtos));
        } catch (Exception e) {
            throw new GetAnnounceException();
        }
    }

    @GetMapping("/detail/{announceId}")
    public ResponseEntity<?> getAnnounceDetail(@PathVariable Integer announceId) {
        try {
            log.info("\n\n 요청 공지 ID {}", announceId);

            AnnounceDto announceDto = announceService.getAnnounceDetail(announceId);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new CustomResponse(GET_ANNOUNCE_DETAIL_SUCCESS.getMessage(), announceDto));
        } catch (Exception e) {
            throw new GetAnnounceException();
        }
    }
}
