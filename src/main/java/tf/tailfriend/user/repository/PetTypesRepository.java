package tf.tailfriend.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.user.entity.PetTypes;

public interface PetTypesRepository extends JpaRepository<PetTypes, Integer> {
}
