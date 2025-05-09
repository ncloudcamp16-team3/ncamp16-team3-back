package tf.tailfriend.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tf.tailfriend.board.entity.Board;
import tf.tailfriend.board.entity.BoardBookmark;
import tf.tailfriend.board.entity.BoardLike;

import java.util.List;
import java.util.Optional;

public interface BoardBookmarkDao extends JpaRepository<BoardBookmark, BoardBookmark.BoardBookmarkId> {

    // 사용자 ID로 게시글 북마크 목록을 조회
    List<BoardBookmark> findByUserId(Integer userId);

    List<BoardBookmark> findByUserIdAndBoardBoardTypeId(Integer userId, Integer boardTypeId);

    Optional<BoardBookmark> findByIdUserIdAndIdBoardPostId(Integer userId, Integer boardPostId);

    void deleteAllByBoard(Board board);

    List<BoardBookmark> findAllByBoard(Board board);

    @Modifying
    @Query("DELETE FROM BoardBookmark bb WHERE bb.user.id = :userId")
    void deleteByUserId(@Param("userId") Integer userId);


    @Query("SELECT b FROM BoardBookmark b JOIN FETCH b.board WHERE b.user.id = :userId")
    List<BoardBookmark> findAllByUserIdWithBoard(@Param("userId") Integer userId);

}

