package tf.tailfriend.petsta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.petsta.entity.PetstaPost;
import tf.tailfriend.user.entity.User;

import java.util.Optional;

public interface PetstaPostDao extends JpaRepository<PetstaPost, Integer> {


}
