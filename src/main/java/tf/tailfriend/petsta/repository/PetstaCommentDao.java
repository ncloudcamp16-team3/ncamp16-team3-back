package tf.tailfriend.petsta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.petsta.entity.PetstaComment;
import tf.tailfriend.petsta.entity.PetstaPost;

import java.util.List;
import java.util.Optional;

public interface PetstaCommentDao extends JpaRepository<PetstaComment, Integer> {
    List<PetstaComment> findByPostAndParentIsNullOrderByCreatedAtDesc(PetstaPost post);

    List<PetstaComment> findByParentOrderByCreatedAtAsc(PetstaComment parentComment);
}
