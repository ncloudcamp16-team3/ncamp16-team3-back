package tf.tailfriend.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.user.entity.Pets;

public interface PetsRepository extends JpaRepository<Pets, Integer> {
    // 특정 유저의 반려동물 조회
    // List<Pets> findByOwnerId(Integer ownerId);
}