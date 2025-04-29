package tf.tailfriend.petsta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.petsta.entity.PetstaBookmark;
import tf.tailfriend.petsta.entity.PetstaLike;

import java.util.Optional;

public interface PetstaBookmarkDao extends JpaRepository<PetstaBookmark, Integer> {
    Optional<PetstaBookmark> findByUserIdAndPetstaPostId(Integer userId, Integer petstaPostId);

    boolean existsByUserIdAndPetstaPostId(Integer loginUserId, Integer id);
}
