package tf.tailfriend.petsitter.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.petsitter.entity.PetSitter;

public interface PetSitterDao extends JpaRepository<PetSitter, Integer> {

    Page<PetSitter> findByStatus(PetSitter.PetSitterStatus status, Pageable pageable);

    Page<PetSitter> findByUserNicknameContainingAndStatusEquals(
            String nickname, PetSitter.PetSitterStatus status, Pageable pageable);

    Page<PetSitter> findByAgeContainingAndStatusEquals(
            String age, PetSitter.PetSitterStatus status, Pageable pageable);

    Page<PetSitter> findByHouseTypeContainingAndStatusEquals(
            String houseType, PetSitter.PetSitterStatus status, Pageable pageable);

    Page<PetSitter> findByCommentContainingAndStatusEquals(
            String comment, PetSitter.PetSitterStatus status, Pageable pageable);

}
