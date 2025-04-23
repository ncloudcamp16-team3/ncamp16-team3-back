package tf.tailfriend.petmeeting.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tf.tailfriend.pet.entity.Pet;

import java.util.List;

public interface PetmeetingDAO extends JpaRepository<Pet, Integer> {

    @Query("SELECT p FROM Pet p " +
            "WHERE p.user.dongName IN :dongNames " +
            "AND p.activityStatus = :activityStatus " +
            "ORDER BY p.name")
    Page<Pet> findByDongNamesAndActivityStatus(
            @Param("dongNames") List<String> dongNames,
            @Param("activityStatus") Pet.ActivityStatus activityStatus,
            Pageable pageable);

    @Query("SELECT p FROM Pet p " +
            "WHERE p.activityStatus = :activityStatus")
    Page<Pet> findByActivityStatus(Pet.ActivityStatus activityStatus, Pageable pageable);

}
