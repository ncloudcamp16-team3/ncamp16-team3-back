package tf.tailfriend.pet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tf.tailfriend.pet.entity.PetPhoto;
import tf.tailfriend.petmeeting.dto.PetPhotoDTO;

import java.util.List;

public interface PetPhotoDao extends JpaRepository<PetPhoto, PetPhoto.PetPhotoId> {
    @Query("SELECT new tf.tailfriend.petmeeting.dto.PetPhotoDTO(f.id, f.path, p.thumbnail) " +
            "FROM PetPhoto p JOIN p.file f WHERE p.pet.id = :petId")
    List<PetPhotoDTO> findByPetId(@Param("petId") Integer petId);

    @Modifying
    @Query("DELETE FROM PetPhoto pp WHERE pp.pet.id = :petId")
    void deleteByPetId(@Param("petId") Integer petId);

}
