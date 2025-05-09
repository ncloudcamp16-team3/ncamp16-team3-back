package tf.tailfriend.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tf.tailfriend.board.entity.Board;
import tf.tailfriend.board.entity.BoardLike;

import java.util.List;
import java.util.Optional;

public interface BoardLikeDao extends JpaRepository<BoardLike, BoardLike.BoardLikeId> {
    Optional<BoardLike> findByIdUserIdAndIdBoardPostId(Integer userId, Integer boardPostId);

    void deleteAllByBoard(Board board);

    @Modifying
    @Query("DELETE FROM BoardLike bl WHERE bl.user.id = :userId")
    void deleteByUserId(@Param("userId") Integer userId);

    @Query("SELECT bl FROM BoardLike bl JOIN FETCH bl.board WHERE bl.user.id = :userId")
    List<BoardLike> findAllByUserIdWithBoard(@Param("userId") Integer userId);
}
