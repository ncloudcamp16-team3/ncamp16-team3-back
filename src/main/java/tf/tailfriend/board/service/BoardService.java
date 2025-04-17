package tf.tailfriend.board.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tf.tailfriend.board.dto.BoardResponseDto;
import tf.tailfriend.board.entity.Board;
import tf.tailfriend.board.repository.BoardDao;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardDao boardDao;

    @Transactional(readOnly = true)
    public Page<BoardResponseDto> getAllBoards(Pageable pageable) {
        Page<Board> boards = boardDao.findAll(pageable);
        return boards.map(BoardResponseDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<BoardResponseDto> getBoardsByType(Integer boardTypeId, Pageable pageable) {
        Page<Board> boards = boardDao.findByBoardTypeId(boardTypeId, pageable);
        return boards.map(BoardResponseDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public BoardResponseDto getBoardById(Integer boardId) {
        Board board = boardDao.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다: " + boardId));
        return BoardResponseDto.fromEntity(board);
    }
}
