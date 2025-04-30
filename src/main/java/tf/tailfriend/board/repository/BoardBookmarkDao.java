package tf.tailfriend.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.board.entity.Board;
import tf.tailfriend.board.entity.BoardBookmark;

import java.util.List;
import java.util.Optional;

public interface BoardBookmarkDao extends JpaRepository<BoardBookmark, BoardBookmark.BoardBookmarkId> {
    Optional<BoardBookmark> findByIdUserIdAndIdBoardPostId(Integer userId, Integer boardPostId);

    void deleteAllByBoard(Board board);

    List<BoardBookmark> findAllByBoard(Board board);
}
