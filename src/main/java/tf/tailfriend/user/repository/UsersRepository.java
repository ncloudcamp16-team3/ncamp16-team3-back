package tf.tailfriend.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tf.tailfriend.user.entity.Users;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Integer> {
    // 필요 시 추가로 사용자 조회 메서드 정의 가능
    Optional<Users> findBySnsAccountId(String snsAccountId);

}
