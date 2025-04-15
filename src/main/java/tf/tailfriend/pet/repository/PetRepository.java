package tf.tailfriend.pet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.pet.entity.Pet;

public interface PetRepository extends JpaRepository<Pet, Integer> {

}