package tf.tailfriend.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.user.entity.Files;

public interface FilesRepository extends JpaRepository<Files, Integer> {

}
