package tf.tailfriend.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.user.entity.PetPhoto;
import tf.tailfriend.user.entity.PetPhotoId;

import java.util.List;

public interface PetPhotoRepository extends JpaRepository<PetPhoto, PetPhotoId> {

    // 특정 반려동물의 모든 사진 조회
    List<PetPhoto> findByIdPetId(Integer petId);

    // 특정 반려동물의 대표 사진만 조회
    PetPhoto findByIdPetIdAndThumbnailTrue(Integer petId);
}
