package tf.tailfriend.schedule.entity.dto;

import jakarta.persistence.*;
import lombok.*;
import tf.tailfriend.user.entity.User;

import java.time.LocalDateTime;


public class ScheduleDTO {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public class ScheduleAllGetDTO {
        private Integer id;
        private User user;
        private String title;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String address;
        private Double latitude;
        private Double longitude;
    }

}
