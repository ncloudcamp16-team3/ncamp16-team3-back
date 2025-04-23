package tf.tailfriend.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tf.tailfriend.board.entity.BoardType;

import java.util.Optional;

@Repository
public interface BoardTypeDao extends JpaRepository<BoardType, Integer> {

    Optional<BoardType> findByName(String name);
}
