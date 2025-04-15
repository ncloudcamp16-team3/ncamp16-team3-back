package tf.tailfriend.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "files")
public class Files {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileType type = FileType.PHOTO;

    @Column(length = 50)
    private String path;

    @Column(length = 255)
    private String uuid;

    public enum FileType {
        PHOTO, VIDEO
    }

}

