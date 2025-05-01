package tf.tailfriend.facility.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CurrentTimestamp;

import java.sql.Time;

@Entity
@Table(name = "facility_timetables")
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class FacilityTimetable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Day day;

    @Column(name = "open_time", nullable = false)
    private Time openTime;

    @Column(name = "close_time", nullable = false)
    private Time closeTime;

    @Getter
    public enum Day {
        MON("MON"),
        TUE("TUE"),
        WED("WED"),
        THU("THU"),
        FRI("FRI"),
        SAT("SAT"),
        SUN("SUN");

        private final String value;

        Day(String value) {
            this.value = value;
        }
    }
}
