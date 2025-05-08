package tf.tailfriend.board.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.board.entity.Board;
import tf.tailfriend.board.entity.Product;

public interface ProductDao extends JpaRepository<Product, Integer> {
    void deleteByBoard(Board board);
}
