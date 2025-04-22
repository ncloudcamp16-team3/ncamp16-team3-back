package tf.tailfriend.pet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.pet.entity.PetType;

import java.util.Optional;

public interface PetTypeDao extends JpaRepository<PetType, Integer> {
    Optional<PetType> findByName(String name);
}
