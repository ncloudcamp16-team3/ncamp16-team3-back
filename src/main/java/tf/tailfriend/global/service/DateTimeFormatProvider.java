package tf.tailfriend.global.service;

import tf.tailfriend.facility.entity.FacilityTimetable;

import java.sql.Time;
import java.util.List;

public interface DateTimeFormatProvider {
    // 인터페이스에 추가된 메서드들
    String simplifyDays(List<FacilityTimetable> days);
    String formatTime(Time time);
    String getKorDayName(FacilityTimetable.Day day);
    String getKorDayName(int number);
    String timeParser(Time openTime, Time closeTime);

}