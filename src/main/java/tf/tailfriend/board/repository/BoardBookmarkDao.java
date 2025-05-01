package tf.tailfriend.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.board.entity.Board;
import org.springframework.stereotype.Repository;
import tf.tailfriend.board.entity.BoardBookmark;

import java.util.List;
import java.util.Optional;

public interface BoardBookmarkDao extends JpaRepository<BoardBookmark, BoardBookmark.BoardBookmarkId> {

    // 사용자 ID로 게시글 북마크 목록을 조회
    List<BoardBookmark> findByUserId(Integer userId);

    //사용자 ID와 게시글 ID로 게시글 북마크를 조회
    Optional<BoardBookmark> findByUserIdAndBoardId(Integer userId, Integer boardId);

    // 사용자 ID와 게시글 ID로 게시글 북마크 존재 여부
    boolean existsByUserIdAndBoardId(Integer userId, Integer boardId);

    Optional<BoardBookmark> findByIdUserIdAndIdBoardPostId(Integer userId, Integer boardPostId);

    void deleteAllByBoard(Board board);

    List<BoardBookmark> findAllByBoard(Board board);
}

