package tf.tailfriend.pet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tf.tailfriend.pet.entity.PetPhoto;


public interface PetPhotoDao extends JpaRepository<PetPhoto, PetPhoto.PetPhotoId> {

    @Modifying
    @Query("DELETE FROM PetPhoto pp WHERE pp.pet.id = :petId")
    void deleteByPetId(@Param("petId") Integer petId);

}
