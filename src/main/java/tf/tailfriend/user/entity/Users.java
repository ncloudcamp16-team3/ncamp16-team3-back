package tf.tailfriend.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 50, nullable = false, unique = true)
    private String nickname;

    @Column(name = "sns_account_id", length = 255, nullable = false)
    private String snsAccountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sns_type_id", nullable = false)
    private SnsType snsType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = true)
    private Files file;

    @Column(length = 255)
    private String address;

    @Column(name = "detail_address")
    private String detailAddress;

    @Column(name = "dong_name", length = 255)
    private String dongName;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Pets> pets = new ArrayList<>();
}