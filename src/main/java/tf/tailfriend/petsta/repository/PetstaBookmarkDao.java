package tf.tailfriend.petsta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tf.tailfriend.petsta.entity.PetstaBookmark;

import java.util.List;
import java.util.Optional;

public interface PetstaBookmarkDao extends JpaRepository<PetstaBookmark, Integer> {
    Optional<PetstaBookmark> findByUserIdAndPetstaPostId(Integer userId, Integer petstaPostId);

    boolean existsByUserIdAndPetstaPostId(Integer loginUserId, Integer id);

    List<PetstaBookmark> findByUserId(Integer userId);

    @Query("SELECT pb FROM PetstaBookmark pb WHERE pb.user.id = :userId AND pb.petstaPost.deleted = false")
    List<PetstaBookmark> findByUserIdAndNotDeleted(@Param("userId") Integer userId);

    @Modifying
    @Query("DELETE FROM PetstaBookmark pb WHERE pb.user.id = :userId")
    void deleteByUserId(@Param("userId") Integer userId);

    @Query("SELECT pb FROM PetstaBookmark pb JOIN pb.petstaPost p JOIN p.user u WHERE pb.user.id = :userId AND u.deleted = false")
    List<PetstaBookmark> findByUserIdAndPetstaPostUserNotDeleted(@Param("userId") Integer userId);

    @Query("SELECT b FROM PetstaBookmark b JOIN FETCH b.petstaPost WHERE b.user.id = :userId")
    List<PetstaBookmark> findAllByUserIdWithPost(@Param("userId") Integer userId);

    @Modifying
    @Query("DELETE FROM PetstaBookmark b WHERE b.petstaPost.id = :postId")
    void deleteAllByPostId(@Param("postId") Integer postId);

}
