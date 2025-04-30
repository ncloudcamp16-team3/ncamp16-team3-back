package tf.tailfriend.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.board.entity.Board;
import tf.tailfriend.board.entity.BoardLike;

import java.util.Optional;

public interface BoardLikeDao extends JpaRepository<BoardLike, BoardLike.BoardLikeId> {
    Optional<BoardLike> findByIdUserIdAndIdBoardPostId(Integer userId, Integer boardPostId);

    void deleteAllByBoard(Board board);
}
