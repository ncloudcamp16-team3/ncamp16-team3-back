package tf.tailfriend.petsitter.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.petsitter.entity.PetSitter;

public interface PetSitterDao extends JpaRepository<PetSitter, Integer> {

    Page<PetSitter> findByApplyAtIsNotNull(Pageable pageable);

    Page<PetSitter> findByApplyAtIsNull(Pageable pageable);
}
