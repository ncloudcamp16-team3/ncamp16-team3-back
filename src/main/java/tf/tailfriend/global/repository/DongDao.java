package tf.tailfriend.global.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tf.tailfriend.global.entity.Dong;

import java.util.List;
import java.util.Optional;

public interface DongDao extends JpaRepository<Dong, Integer> {

    @Query(value = """
    SELECT d.name
    FROM dongs d
    ORDER BY ST_Distance_Sphere(
        POINT(:longitude, :latitude),
        POINT(d.longitude, d.latitude)
    )
    LIMIT :count
    """, nativeQuery = true)
    List<String> findNearbyDongs(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("count") int count
    );

    Optional<Dong> findByName(@Param("name") String name);
}
