package tf.tailfriend.facility.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CurrentTimestamp;
import tf.tailfriend.board.entity.BoardPhoto;
import tf.tailfriend.facility.entity.dto.ResponseForReserve.FacilityCard;
import tf.tailfriend.facility.entity.dto.ResponseForReserve.FacilityWithDistanceProjection;

import java.sql.Time;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "facilities")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
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

    @CurrentTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "facility", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FacilityPhoto> photos = new ArrayList<>();

    @OneToMany(mappedBy = "facility", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FacilityTimetable> timetables = new ArrayList<>();

    @Transient
    private Double distance;

    public void addTimetable(FacilityTimetable.Day day, Time openTime, Time closeTime) {
        FacilityTimetable timetable = FacilityTimetable.builder()
                .day(day)
                .openTime(openTime)
                .closeTime(closeTime)
                .facility(this)
                .build();

        timetables.add(timetable);
    }

    // 두 지점 간의 거리를 계산하는 메서드 (예시: Haversine 공식을 이용한 거리 계산)
    public void setDistance(Double userLatitude, Double userLongitude) {
        double earthRadius = 6371; // 지구 반지름 (킬로미터 단위)

        double latDistance = Math.toRadians(userLatitude - this.latitude);
        double lonDistance = Math.toRadians(userLongitude - this.longitude);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(userLatitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        this.distance = earthRadius * c; // 거리 (킬로미터 단위)
    }
}
