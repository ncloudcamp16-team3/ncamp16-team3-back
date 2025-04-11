package tf.tailfriend.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.user.entity.PetPhotosId;
import tf.tailfriend.user.entity.PetPhotos;

public interface PetPhotosRepository extends JpaRepository<PetPhotos, PetPhotosId> {


}
