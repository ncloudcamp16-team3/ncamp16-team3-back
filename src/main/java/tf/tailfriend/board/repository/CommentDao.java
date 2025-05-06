package tf.tailfriend.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tf.tailfriend.board.entity.Board;
import tf.tailfriend.board.entity.Comment;

import java.util.List;

@Repository
public interface CommentDao extends JpaRepository<Comment, Integer> {

    List<Comment> findByBoardId(Integer boardId);

    List<Comment> findByBoardIdOrderByCreatedAtDesc(Integer boardId);

    long countByBoardId(Integer boardId);

    List<Comment> findByBoardIdAndParentIdIsNull(Integer boardId);

    @Modifying
    void deleteAllByBoard(Board board);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.user.id = :userId")
    void deleteByUserId(@Param("userId") Integer userId);

    List<Comment> findRepliesByParentId(Integer parentId);
}
