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

    @Modifying
    @Query("UPDATE Comment c SET c.refComment = NULL WHERE c.user.id = :userId OR c.refComment.id IN (SELECT c2.id FROM Comment c2 WHERE c2.user.id = :userId)")
    void clearRefCommentIdByUser(@Param("userId") Integer userId);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.parent.id IN (SELECT c2.id FROM Comment c2 WHERE c2.user.id = :userId)")
    void deleteChildCommentsByParentUserId(@Param("userId") Integer userId);
    @Modifying
    @Query(value = "?1", nativeQuery = true)
    void executeNativeQuery(String sql, Object... params);
    List<Comment> findRepliesByParentId(Integer parentId);

    @Query("SELECT c FROM Comment c JOIN FETCH c.board WHERE c.user.id = :userId AND c.deleted = false")
    List<Comment> findAllActiveByUserIdWithBoard(@Param("userId") Integer userId);
}
