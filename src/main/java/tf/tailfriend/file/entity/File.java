package tf.tailfriend.file.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "files")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileType type = FileType.PHOTO;

    @Column(nullable = false)
    private String path;

    @Column(nullable = false)
    private String uuid;

    public enum FileType {
        PHOTO, VIDEO
    }
}
