package tf.tailfriend.pet.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tf.tailfriend.pet.entity.Pet;
import tf.tailfriend.pet.entity.dto.PetFriendDto;
import tf.tailfriend.user.entity.User;

import java.util.List;

public interface PetDao extends JpaRepository<Pet, Integer> {
    @Query(value = """
     SELECT p.id as id, p.name as name, p.gender as gender, p.birth as birth, p.weight as weight,
               p.info as info, p.neutered as neutered, p.activity_status as activityStatus,
            u.id as ownerId, u.nickname as nickname, u.address as address, u.dong_name as dongName,
                u.latitude as latitude, u.longitude as longitude,
               ST_Distance_Sphere(POINT(u.longitude, u.latitude), POINT(:longitude, :latitude)) as distance
         FROM pets p
    JOIN users u ON p.owner_id = u.id
    WHERE u.dong_name IN :dongNames
      AND p.activity_status = :activityStatus
      AND u.latitude IS NOT NULL
      AND u.longitude IS NOT NULL 
      AND p.owner_id != :myId
    ORDER BY distance
    """,
            countQuery = """
    SELECT COUNT(*) FROM pets p
    JOIN users u ON p.owner_id = u.id
    WHERE u.dong_name IN :dongNames
      AND p.activity_status = :activityStatus
      AND u.latitude IS NOT NULL
      AND u.longitude IS NOT NULL 
      AND p.owner_id != :myId
    """,
            nativeQuery = true)
    Page<PetFriendDto> findByDongNamesAndActivityStatus(
            @Param("dongNames") List<String> dongNames,
            @Param("activityStatus") String activityStatus,
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            Pageable pageable,
            @Param("myId") Integer myId);

    List<Pet> findByUserId(Integer userId);

    List<Pet> findAllByUserId(Integer userId);
}