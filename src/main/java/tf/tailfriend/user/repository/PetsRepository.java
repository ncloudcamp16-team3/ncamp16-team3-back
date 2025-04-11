package tf.tailfriend.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.user.entity.Pets;

public interface PetsRepository extends JpaRepository<Pets, Integer> {

}