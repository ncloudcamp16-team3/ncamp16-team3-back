package tf.tailfriend.pet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.pet.entity.Pet;
import tf.tailfriend.pet.entity.PetMatch;

public interface PetMatchDao extends JpaRepository<PetMatch, Integer> {
    boolean existsByPet1IdAndPet2Id(Integer minId, Integer maxId);
}
