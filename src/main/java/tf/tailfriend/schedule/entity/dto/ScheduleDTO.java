package tf.tailfriend.schedule.entity.dto;

;
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
        private String title;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String address;
        private Double latitude;
        private Double longitude;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SchedulePutDTO {
        private Integer id;
        private Integer userId;
        private String title;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String address;
        private Double latitude;
        private Double longitude;
    }


}
