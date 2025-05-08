package tf.tailfriend.petsta.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tf.tailfriend.petsta.entity.PetstaPost;

import java.util.List;
import java.util.Optional;

public interface PetstaPostDao extends JpaRepository<PetstaPost, Integer> {

    Page<PetstaPost> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Modifying
    @Query("update PetstaPost p set p.likeCount = p.likeCount + 1 where p.id = :id")
    void incrementLikeCount(@Param("id") Integer postId);


    @Modifying
    @Query("update PetstaPost p set p.likeCount = p.likeCount - 1 where p.id = :id")
    void decrementLikeCount(@Param("id") Integer postId);

    @Modifying
    @Query("update PetstaPost p set p.commentCount = p.commentCount + 1 where p.id = :id")
    void incrementCommentCount(@Param("id") Integer postId);

    @Modifying
    @Query("update PetstaPost p set p.commentCount = p.commentCount - 1 where p.id = :id and p.commentCount > 0")
    void decrementCommentCount(@Param("id") Integer postId);

    List<PetstaPost> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(Integer userId);

    List<PetstaPost> findByUserIdOrderByCreatedAtDesc(Integer userId);

    Page<PetstaPost> findAllByDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

    Optional<PetstaPost> findByIdAndDeletedFalse(Integer postId);

    PetstaPost getPetstaPostById(Integer postId);

    @Modifying
    @Query("DELETE FROM PetstaPost pp WHERE pp.user.id = :userId")
    void deleteByUserId(@Param("userId") Integer userId);

    // 게시글 ID 리스트 조회
    @Query("SELECT p.id FROM PetstaPost p WHERE p.user.id = :userId")
    List<Integer> findIdsByUserId(@Param("userId") Integer userId);

    List<PetstaPost> findAllByUserId(Integer userId);
}

