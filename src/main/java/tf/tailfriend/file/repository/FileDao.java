package tf.tailfriend.file.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.file.entity.File;

public interface FileDao extends JpaRepository<File, Integer> {
    File getFileById(int i);
}