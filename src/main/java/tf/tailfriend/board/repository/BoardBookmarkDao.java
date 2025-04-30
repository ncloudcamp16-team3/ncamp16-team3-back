package tf.tailfriend.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.board.entity.BoardBookmark;

import java.util.Optional;

public interface BoardBookmarkDao extends JpaRepository<BoardBookmark, BoardBookmark.BoardBookmarkId> {
    Optional<BoardBookmark> findByIdUserIdAndIdBoardPostId(Integer userId, Integer boardPostId);
}
