package tf.tailfriend.board.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tf.tailfriend.board.entity.Board;

@Repository
public interface BoardDao extends JpaRepository<Board, Integer> {

    Page<Board> findByBoardTypeId(Integer boardTypeId, Pageable pageable);
}
