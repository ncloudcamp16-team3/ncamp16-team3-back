package tf.tailfriend.facility.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CurrentTimestamp;

import java.sql.Time;
import java.time.LocalDateTime;
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

    @Column(name = "total_star_point", nullable = false)
    private Integer totalStarPoint = 0;

    @Column(name = "review_count", nullable = false)
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

    public void updateInformation(
            FacilityType facilityType,
            String name,
            String tel,
            String address,
            String detailAddress,
            String comment,
            Double latitude,
            Double longitude) {

        this.facilityType = facilityType;
        this.name = name;
        this.tel = tel;
        this.address = address;
        this.detailAddress = detailAddress;
        this.comment = comment;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void updateTotalStarPoint(Integer starPoint) {
        this.totalStarPoint += starPoint;
    }

    public void updateReviewCount() {
        this.reviewCount += 1;
    }

    public void discountReview() {this.reviewCount -= 1;}
}
