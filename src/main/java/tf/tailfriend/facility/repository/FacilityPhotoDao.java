package tf.tailfriend.facility.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tf.tailfriend.facility.entity.FacilityPhoto;
import tf.tailfriend.facility.entity.dto.forReserve.ThumbnailForCardDto;

import java.util.List;
import java.util.Map;

import java.util.List;

public interface FacilityPhotoDao extends JpaRepository<FacilityPhoto, Integer> {

    List<FacilityPhoto> findByFacilityId(Integer id);

    @Query("""
    SELECT new tf.tailfriend.facility.entity.dto.forReserve.ThumbnailForCardDto(fp.facility.id as facilityId, f.path as path)
    FROM FacilityPhoto fp
    JOIN fp.file f
    WHERE fp.facility.id IN :facilityIds
    AND fp.id.fileId = (
        SELECT MIN(fp2.id.fileId)
        FROM FacilityPhoto fp2
        WHERE fp2.facility.id = fp.facility.id
    )
""")
    List<ThumbnailForCardDto> findThumbnailPathByFacilityIds(@Param("facilityIds") List<Integer> facilityIds);

}
