package tf.tailfriend.schedule.entity.dto;

import jakarta.persistence.*;
import lombok.*;
import tf.tailfriend.facility.entity.Facility;
import tf.tailfriend.reserve.entity.Payment;
import tf.tailfriend.reserve.entity.Reserve;
import tf.tailfriend.user.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarReserveDTO {

        private Integer id;
        private Integer userId;
        private String title; // 예약시설 이름
        private LocalDateTime entryTime;
        private LocalDateTime exitTime;
        private Integer amount;
        private Boolean reserveStatus;
        private Double latitude;
        private Double longitude;
        private String address;
        private List<LocalDate> dateList;

        public CalendarReserveDTO(Reserve reserve) {
                this.id = reserve.getId();
                this.userId = reserve.getUser().getId();
                this.title = reserve.getFacility().getName();
                this.entryTime = reserve.getEntryTime();
                this.exitTime = reserve.getExitTime();
                this.amount = reserve.getAmount();
                this.reserveStatus = reserve.getReserveStatus();
                this.dateList = getDatesBetween(reserve.getEntryTime().toLocalDate(), reserve.getExitTime().toLocalDate());
                this.latitude = reserve.getFacility().getLatitude();
                this.longitude= reserve.getFacility().getLongitude();
                this.address=reserve.getFacility().getAddress();
        }

        private List<LocalDate> getDatesBetween(LocalDate start, LocalDate end) {
                return start.datesUntil(end.plusDays(1)) // end 포함하려면 plusDays(1)
                        .collect(Collectors.toList());
        }
}
