package tf.tailfriend.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tf.tailfriend.board.dto.BoardResponseDto;
import tf.tailfriend.board.service.BoardService;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminBoardController {

    private final BoardService boardService;

    @GetMapping("/board/list")
    public ResponseEntity<?> boardList(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false) Integer boardTypeId) {

        try {
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").descending());

            Page<BoardResponseDto> boards;

            if (boardTypeId != null) {
                boards = boardService.getBoardsByType(boardTypeId, pageRequest);
            } else {
                boards = boardService.getAllBoards(pageRequest);
            }

            return ResponseEntity.ok(boards);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "게시판 목록 조회 실패: " + e.getMessage()));
        }
    }

    @GetMapping("/board/{id}")
    public ResponseEntity<?> getBoardDetail(@PathVariable Integer id) {
        try {
            BoardResponseDto board = boardService.getBoardById(id);
            return ResponseEntity.ok(board);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "게시글 상세 조회 실패: " + e.getMessage()));
        }
    }
}
