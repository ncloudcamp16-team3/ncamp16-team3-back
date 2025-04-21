package tf.tailfriend.pet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.pet.entity.PetPhoto;


public interface PetPhotoDao extends JpaRepository<PetPhoto, PetPhoto.PetPhotoId> {

}
