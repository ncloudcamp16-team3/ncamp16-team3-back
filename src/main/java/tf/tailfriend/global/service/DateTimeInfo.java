package tf.tailfriend.global.service;

import lombok.Getter;

/**
 * 날짜 및 시간 정보를 담는 불변 객체입니다.
 */
@Getter
public class DateTimeInfo {
    private final String day;
    private final String date;
    private final String time;

    public DateTimeInfo(String day, String date, String time) {
        this.day = day;
        this.date = date;
        this.time = time;
    }

}
