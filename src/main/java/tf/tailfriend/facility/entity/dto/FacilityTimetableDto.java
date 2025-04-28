package tf.tailfriend.facility.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import tf.tailfriend.facility.entity.FacilityTimetable;

import java.sql.Time;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacilityTimetableDto {
    private Integer id;
    private FacilityTimetable.Day day;
    private Time openTime;
    private Time closeTime;

}


