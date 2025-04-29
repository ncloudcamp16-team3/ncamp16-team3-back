package tf.tailfriend.board.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tf.tailfriend.board.dto.BoardResponseDto;
import tf.tailfriend.board.dto.SearchRequestDto;
import tf.tailfriend.board.exception.GetBoardTypeException;
import tf.tailfriend.board.exception.GetPostException;
import tf.tailfriend.board.exception.SearchPostException;
import tf.tailfriend.board.service.BoardService;
import tf.tailfriend.board.service.BoardTypeService;
import tf.tailfriend.board.service.CommentService;
import tf.tailfriend.global.response.CustomResponse;

import static tf.tailfriend.board.message.SuccessMessage.*;

@RestController
@RequestMapping("/api/board")
@RequiredArgsConstructor
@Slf4j
public class BoardController {

    private final BoardService boardService;
    private final BoardTypeService boardTypeService;
    private final CommentService commentService;

    @GetMapping("/detail/{postId}")
    public ResponseEntity<?> getBoardDetail(@PathVariable Integer postId) {

        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new CustomResponse(GET_POST_SUCCESS.getMessage(), boardService.getBoardById(postId)));
        }
        catch (Exception e) {
            throw new GetPostException();
        }
    }

    @GetMapping("/type")
    public ResponseEntity<?> getBoardTypeList() {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new CustomResponse(GET_BOARD_TYPE_SUCCESS.getMessage(), boardTypeService.getBoardTypeList()));
        }
        catch (Exception e) {
            throw new GetBoardTypeException();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchPost(@ModelAttribute SearchRequestDto searchRequestDto
                                        ) {
        log.info("\n검색 요청 Dto: {} ", searchRequestDto);

        try {
            Pageable pageable = PageRequest.of(searchRequestDto.getPage(), searchRequestDto.getSize());

            Page<BoardResponseDto> posts;
            String keyword = searchRequestDto.getKeyword();
            if(keyword == null) {
                posts = boardService.getBoardsByType(searchRequestDto.getBoardTypeId(), pageable);
            }else{
                posts = boardService.getBoardsByTypeAndKeyword(searchRequestDto.getBoardTypeId(), keyword, pageable);
            }

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new CustomResponse(SEARCH_POST_SUCCESS.getMessage(), posts));
        }
        catch (Exception e) {
            throw new SearchPostException();
        }
    }
}
