package tf.tailfriend.pet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tf.tailfriend.pet.entity.Pet;
import tf.tailfriend.pet.entity.PetMatch;

public interface PetMatchDao extends JpaRepository<PetMatch, Integer> {
    boolean existsByPet1IdAndPet2Id(Integer minId, Integer maxId);

    @Modifying
    @Query("DELETE FROM PetMatch pm WHERE pm.pet1.id = :petId OR pm.pet2.id = :petId")
    void deleteByPet1IdOrPet2Id(@Param("petId") Integer petId);
}
