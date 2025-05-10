package tf.tailfriend.petsta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tf.tailfriend.petsta.entity.PetstaComment;
import tf.tailfriend.petsta.entity.PetstaCommentMention;
import tf.tailfriend.petsta.entity.PetstaPost;

import java.util.List;


public interface PetstaCommentDao extends JpaRepository<PetstaComment, Integer> {
    List<PetstaComment> findByPostAndParentIsNullOrderByCreatedAtDesc(PetstaPost post);

    List<PetstaComment> findByParentOrderByCreatedAtAsc(PetstaComment parentComment);

    List<PetstaComment> findRepliesByParentId(Integer parentId);


    // 댓글 삭제
    @Modifying
    @Query("DELETE FROM PetstaComment pc WHERE pc.post.id = :postId")
    void deleteByPostId(@Param("postId") Integer postId);

    @Modifying
    @Query("DELETE FROM PetstaComment c WHERE c.post.id = :postId AND c.parent IS NOT NULL")
    void deleteRepliesByPostId(@Param("postId") Integer postId);

    // 부모 댓글 삭제
    @Modifying
    @Query("DELETE FROM PetstaComment c WHERE c.post.id = :postId AND c.parent IS NULL")
    void deleteParentsByPostId(@Param("postId") Integer postId);

    @Modifying
    @Query("DELETE FROM PetstaComment pc WHERE pc.user.id = :userId")
    void deleteByUserId(@Param("userId") Integer userId);

    @Modifying
    @Query(value = "?1", nativeQuery = true)
    void executeNativeQuery(String sql, Object... params);

    @Query("SELECT c FROM PetstaComment c LEFT JOIN FETCH c.replies WHERE c.user.id = :userId AND c.deleted = false")
    List<PetstaComment> findAllWithRepliesByUserId(@Param("userId") Integer userId);

    @Query("SELECT m FROM PetstaCommentMention m WHERE m.mentionedUser.id = :userId")
    List<PetstaCommentMention> findMentionsByUserId(@Param("userId") Integer userId);
}

