package tf.tailfriend.facility.entity;

import jakarta.persistence.*;
import lombok.*;
import tf.tailfriend.board.entity.BoardPhoto;

import java.sql.Time;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "facilities")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Facility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_type_id", nullable = false)
    private FacilityType facilityType;

    @Column(nullable = false)
    private String name;

    @Column(length = 50)
    private String tel;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "star_point", nullable = false)
    private Double starPoint = 0.0;

    @Column(name= "review_count", nullable = false)
    private Integer reviewCount = 0;

    @Column(nullable = false)
    private String address;

    @Column(name = "detail_address")
    private String detailAddress;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @OneToMany(mappedBy = "facility", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FacilityPhoto> photos = new ArrayList<>();

    @OneToMany(mappedBy = "facility", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FacilityTimetable> timetables = new ArrayList<>();

    public void addTimetable(FacilityTimetable.Day day, Time openTime, Time closeTime) {
        FacilityTimetable timetable = FacilityTimetable.builder()
                .day(day)
                .openTime(openTime)
                .closeTime(closeTime)
                .facility(this)
                .build();

        timetables.add(timetable);
    }
}
