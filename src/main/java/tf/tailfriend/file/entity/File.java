package tf.tailfriend.file.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "files")
@Getter
@Setter
@NoArgsConstructor
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
