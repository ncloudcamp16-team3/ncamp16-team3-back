package tf.tailfriend.facility.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

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
    private FacilityType facilityTypeId;

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
}
