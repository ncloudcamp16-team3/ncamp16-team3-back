package tf.tailfriend.pet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.pet.entity.PetType;

public interface PetTypeDao extends JpaRepository<PetType, Integer> {
}
