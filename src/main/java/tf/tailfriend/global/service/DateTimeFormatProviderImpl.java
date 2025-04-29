package tf.tailfriend.global.service;

import org.springframework.stereotype.Service;
import tf.tailfriend.facility.entity.FacilityTimetable;

import java.sql.Time;
import java.util.List;

@Service
public class DateTimeFormatProviderImpl implements DateTimeFormatProvider {

    /**
     * 영어 요일명을 한글로 변환합니다.
     */
    @Override
    public String getKorDayName(FacilityTimetable.Day day) {
        return switch (day) {
            case MON -> "월";
            case TUE -> "화";
            case WED -> "수";
            case THU -> "목";
            case FRI -> "금";
            case SAT -> "토";
            case SUN -> "일";
        };
    }

    @Override
    public String getKorDayName(int number) {
        return switch (number) {
            case 0 -> "월";
            case 1 -> "화";
            case 2 -> "수";
            case 3 -> "목";
            case 4 -> "금";
            case 5 -> "토";
            case 6 -> "일";
            default -> null;
        };
    }

    /**
     * 시간을 HH:MM 형식으로 포맷팅합니다.
     */
    @Override
    public String formatTime(Time time) {
        if (time == null) {
            return "-";
        }
        return time.toLocalTime().toString().substring(0, 5); // HH:MM 부분만 추출
    }

    /**
     * 요일 목록을 간소화된 표현으로 변환합니다.
     * 예: [월, 화, 수, 목, 금] -> "월-금"
     */
    @Override
    public String simplifyDays(List<FacilityTimetable> days) {
        List<Integer> indexes = days.stream()
                .filter(t -> t.getOpenTime() != null && t.getCloseTime() != null)
                .map(t -> t.getDay().ordinal())
                .sorted()
                .toList();

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < indexes.size(); ) {
            int start = indexes.get(i);
            int end = start;
            while (i + 1 < indexes.size() && indexes.get(i + 1) == indexes.get(i) + 1) {
                end = indexes.get(++i);
            }

            if (result.length() > 0) result.append(", ");

            if (start == end) {
                result.append(getKorDayName(start));
            } else {
                result.append(getKorDayName(start)).append("-").append(getKorDayName(end));
            }
            i++;
        }

        String dayRange = result.toString();
        if (dayRange.equals("월-금")) return "평일";
        if (dayRange.equals("월-일") || dayRange.equals("월-금, 토-일")) return "매일";

        return dayRange;
    }


    @Override
    public String timeParser(Time openTime, Time closeTime) {
        return formatTime(openTime) + " - " + formatTime(closeTime);
    }
}
