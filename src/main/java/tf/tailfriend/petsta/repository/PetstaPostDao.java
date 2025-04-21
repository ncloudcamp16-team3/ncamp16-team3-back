package tf.tailfriend.petsta.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.petsta.entity.PetstaPost;

import java.util.List;
import java.util.Optional;

public interface PetstaPostDao extends JpaRepository<PetstaPost, Integer> {

    Page<PetstaPost> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
