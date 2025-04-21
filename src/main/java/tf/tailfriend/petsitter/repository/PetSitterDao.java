package tf.tailfriend.petsitter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.petsitter.entity.PetSitter;

public interface PetSitterDao extends JpaRepository<PetSitter, Integer> {
}
