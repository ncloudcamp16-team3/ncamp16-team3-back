package tf.tailfriend.petmeeting.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tf.tailfriend.pet.entity.Pet;
import tf.tailfriend.user.entity.User;

public interface PetmeetingDAO extends JpaRepository<Pet, Integer> {

    @Query("SELECT p FROM Pet p " +
            "WHERE p.user.dongName = :dongName " +
            "AND p.activityStatus = :activityStatus")
    Page<Pet> findByDongNameAndActivityStatus(String dongName, Pet.ActivityStatus activityStatus, Pageable pageable);

}
