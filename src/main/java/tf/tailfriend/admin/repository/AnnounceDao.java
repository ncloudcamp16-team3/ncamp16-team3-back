package tf.tailfriend.admin.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tf.tailfriend.admin.entity.Announce;
import tf.tailfriend.board.entity.BoardType;

import java.util.List;

public interface AnnounceDao extends JpaRepository<Announce, Integer> {

    // BoardType별로 공지사항 목록 조회
    List<Announce> findByBoardTypeOrderByCreatedAtDesc(BoardType boardType);

    // BoardType ID별로 공지사항 목록 조회
    List<Announce> findByBoardTypeIdOrderByCreatedAtDesc(Integer boardTypeId);

    // 최근 공지사항 조회 (페이징 적용)
    List<Announce> findAllByOrderByCreatedAtDesc(org.springframework.data.domain.Pageable pageable);

    // 제목으로 공지사항 검색
    List<Announce> findByTitleContainingOrderByCreatedAtDesc(String keyword);

    // 내용으로 공지사항 검색
    List<Announce> findByContentContainingOrderByCreatedAtDesc(String keyword);

    // 제목 또는 내용으로 공지사항 검색
    List<Announce> findByTitleContainingOrContentContainingOrderByCreatedAtDesc(String titleKeyword, String contentKeyword);

    Page<Announce> findByTitleContainingAndBoardType(String searchTerm, BoardType boardType, Pageable pageable);

    Page<Announce> findByTitleContaining(String searchTerm, Pageable pageable);

    Page<Announce> findByContentContainingAndBoardType(String searchTerm, BoardType boardType, Pageable pageable);

    Page<Announce> findByContentContaining(String searchTerm, Pageable pageable);

    Page<Announce> findByBoardType(BoardType boardType, Pageable pageable);
}
