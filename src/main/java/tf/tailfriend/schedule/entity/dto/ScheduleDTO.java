package tf.tailfriend.schedule.entity.dto;

;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import tf.tailfriend.schedule.entity.Schedule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ScheduleDTO {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleGetDTO {
        private Integer id;
        private Integer userId;
        private String title;
        private String content;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String address;
        private Double latitude;
        private Double longitude;
        private List<LocalDate> dateList; // ✅ 추가

        public ScheduleGetDTO(Schedule schedule) {
            this.id = schedule.getId();
            this.userId = schedule.getUser().getId();
            this.title = schedule.getTitle();
            this.content = schedule.getContent();
            this.startDate = schedule.getStartDate();
            this.endDate = schedule.getEndDate();
            this.address = schedule.getAddress();
            this.latitude = schedule.getLatitude();
            this.longitude = schedule.getLongitude();
            this.dateList = getDatesBetween(schedule.getStartDate().toLocalDate(), schedule.getEndDate().toLocalDate());
        }

        private List<LocalDate> getDatesBetween(LocalDate start, LocalDate end) {
            return start.datesUntil(end.plusDays(1)) // end 포함하려면 plusDays(1)
                    .collect(Collectors.toList());
        }
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SchedulePostDTO {

        @NotNull
        private Integer userId;

        @NotNull
        private String title;

        @NotNull
        private String content;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @NotNull
        private LocalDateTime startDate;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @NotNull
        private LocalDateTime endDate;

        @NotNull
        private String address;

        @NotNull
        private Double latitude;

        @NotNull
        private Double longitude;

        private String fcmToken;

        @AssertTrue(message = "시작일은 종료일보다 이후일 수 없습니다.")
        public boolean isValidDateRange() {
            return startDate == null || endDate == null || !startDate.isAfter(endDate);
        }
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SchedulePutDTO {

        @NotNull
        private Integer id;

        @NotNull
        private Integer userId;

        @NotNull
        private String title;

        @NotNull
        private String content;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @NotNull
        private LocalDateTime startDate;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @NotNull
        private LocalDateTime endDate;

        @NotNull
        private String address;

        @NotNull
        private Double latitude;

        @NotNull
        private Double longitude;

        @AssertTrue(message = "시작일은 종료일보다 이후일 수 없습니다.")
        public boolean isValidDateRange() {
            return startDate == null || endDate == null || !startDate.isAfter(endDate);
        }
    }


}
