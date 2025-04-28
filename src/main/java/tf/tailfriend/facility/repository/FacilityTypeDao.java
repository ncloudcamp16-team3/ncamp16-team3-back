package tf.tailfriend.facility.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tf.tailfriend.facility.entity.FacilityType;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacilityTypeDao extends JpaRepository<FacilityType, Integer> {

//    // 이름으로 시설 타입 조회
//    Optional<FacilityType> findByName(String name);
//
//    // 이름에 특정 텍스트가 포함된 시설 타입 조회
//    List<FacilityType> findByNameContaining(String namePattern);
//
//    // 시설이 등록된 시설 타입만 조회
//    @Query("SELECT DISTINCT ft FROM FacilityType ft JOIN ft.facility f")
//    List<FacilityType> findAllWithFacilities();
//
//    // 시설 수가 많은 순서대로 시설 타입 조회
//    @Query("SELECT ft, COUNT(f) FROM FacilityType ft LEFT JOIN Facility f ON f.facilityType = ft GROUP BY ft ORDER BY COUNT(f) DESC")
//    List<Object[]> findAllOrderByFacilityCount();
}