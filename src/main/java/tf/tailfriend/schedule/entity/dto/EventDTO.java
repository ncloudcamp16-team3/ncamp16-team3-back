package tf.tailfriend.schedule.entity.dto;

import lombok.*;
import tf.tailfriend.schedule.entity.Event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDTO {

    private Integer id;
    private String title;
    private String address;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String eventUrl;
    private Double latitude;
    private Double longitude;
    private List<LocalDate> dateList;

    public EventDTO(Event event) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.address = event.getAddress();
        this.startDate = event.getStartDate();
        this.endDate = event.getEndDate();
        this.eventUrl = event.getEventUrl();
        this.latitude = event.getLatitude();
        this.longitude = event.getLongitude();
        this.dateList = getDatesBetween(event.getStartDate().toLocalDate(), event.getEndDate().toLocalDate());
    }

    private List<LocalDate> getDatesBetween(LocalDate start, LocalDate end) {
        return start.datesUntil(end.plusDays(1)) // end 포함하려면 plusDays(1)
                .collect(Collectors.toList());
    }
}


