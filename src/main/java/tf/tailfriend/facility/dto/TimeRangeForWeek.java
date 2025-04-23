package tf.tailfriend.facility.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import tf.tailfriend.facility.entity.FacilityTimetable;
import tf.tailfriend.global.service.DateTimeFormatProvider;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
public class TimeRangeForWeek {
    private String day;            // 예: "월요일"
    private String openTimeRange; // 예: "09:00 - 18:00", null 가능

    public static List<TimeRangeForWeek> fromEntityList(List<FacilityTimetable> timetables, DateTimeFormatProvider dateTimeFormatProvider) {
        return timetables.stream()
                .map(timetable -> TimeRangeForWeek.builder()
                        .day(dateTimeFormatProvider.getKorDayName(timetable.getDay())) // enum 필드 → 요일명 변환
                        .openTimeRange(timetable.getOpenTime() == null || timetable.getCloseTime() == null ?
                                null :
                                dateTimeFormatProvider.timeParser(
                                        timetable.getOpenTime(),
                                        timetable.getCloseTime()))
                        .build())
                .collect(Collectors.toList());
    }
}