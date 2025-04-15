package tf.tailfriend.file.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.file.entity.File;

public interface FileRepository extends JpaRepository<File, Integer> {
}

