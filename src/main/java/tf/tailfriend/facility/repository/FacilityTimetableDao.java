package tf.tailfriend.facility.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tf.tailfriend.facility.entity.FacilityTimetable;

import java.sql.Time;
import java.util.List;

@Repository
public interface FacilityTimetableDao extends JpaRepository<FacilityTimetable, Integer> {

    // 기존 메소드
    List<FacilityTimetable> findByFacilityId(Integer facilityId);

    // 추가: 여러 시설 ID로 시간표 조회 (N+1 문제 해결용)
    List<FacilityTimetable> findByFacilityIdIn(List<Integer> facilityIds);

    // 요일별 시간표 조회
    List<FacilityTimetable> findByFacilityIdAndDay(Integer facilityId, FacilityTimetable.Day day);

//    // 특정 시간에 열려있는 시설 시간표 조회
//    @Query("SELECT t FROM FacilityTimetable t WHERE t.day = :day AND t.openTime <= :time AND t.closeTime >= :time")
//    List<FacilityTimetable> findOpenFacilitiesByTime(@Param("day") FacilityTimetable.Day day, @Param("time") Time time);
//
//    // 특정 시설이 특정 요일에 영업하는지 확인
//    boolean existsByFacilityIdAndDay(Integer facilityId, FacilityTimetable.Day day);
//
//    // 특정 시설의 영업시간이 가장 긴 요일 조회
//    @Query("SELECT t FROM FacilityTimetable t WHERE t.facility.id = :facilityId ORDER BY (TIME_TO_SEC(t.closeTime) - TIME_TO_SEC(t.openTime)) DESC")
//    List<FacilityTimetable> findLongestOperatingHoursByFacilityId(@Param("facilityId") Integer facilityId, Pageable pageable);
//
//    // 특정 타입의 시설 중 특정 요일에 영업 중인 시설의 시간표 조회
//    @Query("SELECT t FROM FacilityTimetable t JOIN t.facility f JOIN f.facilityType ft WHERE ft.name = :typeName AND t.day = :day")
//    List<FacilityTimetable> findTimetablesByFacilityTypeAndDay(@Param("typeName") String typeName, @Param("day") FacilityTimetable.Day day);
}