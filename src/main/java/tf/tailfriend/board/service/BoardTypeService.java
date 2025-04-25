package tf.tailfriend.board.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tf.tailfriend.board.entity.BoardType;
import tf.tailfriend.board.repository.BoardTypeDao;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BoardTypeService {

    private final BoardTypeDao boardTypeDao;

    @Transactional(readOnly = true)
    public BoardType getBoardTypeById(Integer id) {
        return boardTypeDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시판 타입입니다"));
    }

    @Transactional(readOnly = true)
    public Optional<BoardType> getBoardTypeByName(String name) {
        return boardTypeDao.findByName(name);
    }

    @Transactional(readOnly = true)
    public List<BoardType> getBoardTypeList() {
        return boardTypeDao.findAll();
    }
}
