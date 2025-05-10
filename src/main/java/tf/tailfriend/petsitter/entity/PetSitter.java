package tf.tailfriend.petsitter.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import tf.tailfriend.file.entity.File;
import tf.tailfriend.pet.entity.PetType;
import tf.tailfriend.user.entity.User;

import java.time.LocalDateTime;
import java.util.Arrays;

@Entity
@Table(name = "pet_sitters")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetSitter {

    @Id
    @Column(name = "id")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_type_id")
    private PetType petType;

    @Column(name = "pet_types_formatted")
    private String petTypesFormatted;

    @Column(nullable = false, length = 50)
    private String age;

    @Column(name = "house_type", nullable = false, length = 50)
    private String houseType;

    private String comment;

    @Column(nullable = false)
    private Boolean grown = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "pet_count")
    private PetCount petCount;

    @Column(name = "sitter_exp", nullable = false)
    private Boolean sitterExp = false;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "file_id", nullable = false)
    private File file;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "apply_at")
    private LocalDateTime applyAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PetSitterStatus status = PetSitterStatus.NONE;

    public void setFile(File file) {
        this.file = file;
    }

    @Getter
    public enum PetCount {
        ONE("1"),
        TWO("2"),
        THREE_PLUS("3+");

        private final String value;

        PetCount(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }


        @JsonCreator
        public static PetCount fromValue(String value) {
            try {
                return PetCount.valueOf(value);
            } catch (IllegalArgumentException e) {
                return Arrays.stream(values())
                        .filter(v -> v.value.equals(value))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Invalid PetCount value: " + value));
            }
        }
    }

    @Getter
    public enum PetSitterStatus {
        PENDING, APPROVE, DELETE, NONE
    }

    public void approve() {
        this.status = PetSitterStatus.APPROVE;
        this.applyAt = LocalDateTime.now();
    }

    public void pending() {
        this.status = PetSitterStatus.PENDING;
        this.applyAt = null; // 신청 날짜 초기화
    }

    public void waitForApproval() {
        this.status = PetSitterStatus.NONE;
        this.applyAt = null;
    }

    public void delete() {
        this.status = PetSitterStatus.DELETE;
    }

    public void updateInformation(String age, String houseType, String comment,
                                  Boolean grown, PetCount petCount, Boolean sitterExp,
                                  File file, PetType petType) {
        this.age = age;
        this.houseType = houseType;
        this.comment = comment;
        this.grown = grown;
        this.petCount = petCount;
        this.sitterExp = sitterExp;
        if (file != null) {
            this.file = file;
        }
        if (petType != null) {
            this.petType = petType;
        }
    }
}