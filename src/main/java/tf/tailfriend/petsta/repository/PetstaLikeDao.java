package tf.tailfriend.petsta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tf.tailfriend.petsta.entity.PetstaLike;

import java.util.Optional;

public interface PetstaLikeDao extends JpaRepository<PetstaLike, Integer> {
    Optional<PetstaLike> findByUserIdAndPetstaPostId(Integer userId, Integer petstaPostId);

    boolean existsByUserIdAndPetstaPostId(Integer loginUserId, Integer id);

    @Modifying
    @Query("DELETE FROM PetstaLike pl WHERE pl.user.id = :userId")
    void deleteByUserId(@Param("userId") Integer userId);
}
