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
            @RequestParam(required = false) Integer boardTypeId,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false, defaultValue = "all") String searchField
    ) {
        try {
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").descending());
            Page<BoardResponseDto> boards;
//            log.info("page: {}, size: {}, boardTypeId: {}, searchTerm: {}, searchField: {}", page, size, boardTypeId, searchTerm, searchField);

            // 검색어가 있는 경우 검색 로직 실행
            if (searchTerm != null && !searchTerm.isEmpty()) {
                boards = boardService.searchBoards(searchTerm, searchField, boardTypeId, pageRequest);
            }
            // 게시판 타입 필터링만 있는 경우
            else if (boardTypeId != null) {
                boards = boardService.getBoardsByType(boardTypeId, pageRequest);
            }
            // 아무 조건 없는 경우 전체 조회
            else {
//                log.info("여기로 들어와야함");
                boards = boardService.getAllBoards(pageRequest);
            }

//            log.info("boards: {}", boards.getContent());

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

    @DeleteMapping("/board/{id}/delete")
    public ResponseEntity<?> deleteBoard(@PathVariable Integer id) {
        boardService.deleteBoardById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "게시글 삭제 완료"));
    }
}
