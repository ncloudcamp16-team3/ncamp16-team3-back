package tf.tailfriend.facility.entity.dto.ResponseForReserve;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.sql.Time;

@Getter
@Builder
@AllArgsConstructor
public class FacilitySimpleDto {

    private Integer id;
    private String name;
    private Double starPoint;
    private Integer reviewCount;
    private Double longitude;
    private Double latitude;
    private String address;
    private String facilityTypeName;
    private Double distance;
    private TimetableDto timetable;   // ← 리스트 아니고 하나만
    private String photoPath;          // ← 사진도 대표 하나만 (예쁘게 정리)

    @Getter
    @Builder
    @AllArgsConstructor
    public static class TimetableDto {
        private String day;
        private Time openTime;
        private Time closeTime;
    }
}

