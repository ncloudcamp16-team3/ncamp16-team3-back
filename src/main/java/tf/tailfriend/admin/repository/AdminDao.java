package tf.tailfriend.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tf.tailfriend.admin.entity.Admin;

import java.util.Optional;

@Repository
public interface AdminDao extends JpaRepository<Admin, Integer> {

    Optional<Admin> findByEmail(String email);

}
