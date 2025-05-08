package tf.tailfriend.board.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tf.tailfriend.board.entity.Board;
import tf.tailfriend.board.entity.BoardType;

import java.util.List;

@Repository
public interface BoardDao extends JpaRepository<Board, Integer> {

    Page<Board> findAll(Pageable pageable);

    Page<Board> findByBoardTypeOrderByCreatedAtDesc(BoardType boardTypeId, Pageable pageable);

    Page<Board> findByTitleContaining(String title, Pageable pageable);

    Page<Board> findByTitleContainingAndBoardTypeOrderByCreatedAtDesc(String title, BoardType boardType, Pageable pageable);

    Page<Board> findByContentContaining(String content, Pageable pageable);

    Page<Board> findByContentContainingAndBoardType(String content, BoardType boardTypeId, Pageable pageable);

    // 작성자 검색
    @Query("SELECT b FROM Board b JOIN b.user u WHERE u.nickname LIKE %:nickname%")
    Page<Board> findByUserNicknameContaining(@Param("nickname") String nickname, Pageable pageable);

    @Query("SELECT b FROM Board b JOIN b.user u WHERE u.nickname LIKE %:nickname% AND b.boardType = :boardType")
    Page<Board> findByUserNicknameContainingAndBoardType(
            @Param("nickname") String nickname,
            @Param("boardType") BoardType boardType,
            Pageable pageable);


    Board getBoardById(Integer boardId);

    List<Board> findByUserIdOrderByCreatedAtDesc(Integer userId);

    Page<Board> findByUserIdOrderByCreatedAtDesc(Integer userId, Pageable pageable);

    @Query("SELECT b FROM Board b LEFT JOIN FETCH b.photos WHERE b.user.id = :userId")
    List<Board> findAllByUserIdWithPhotos(@Param("userId") Integer userId);

}
