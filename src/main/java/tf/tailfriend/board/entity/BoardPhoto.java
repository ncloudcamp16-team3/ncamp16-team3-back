package tf.tailfriend.board.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tf.tailfriend.file.entity.File;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "board_photos")
@Getter
@Setter
@NoArgsConstructor
public class BoardPhoto {

    @EmbeddedId
    private BoardPhotoId id;

    @MapsId("boardId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @MapsId("fileId")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private File file;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    public static class BoardPhotoId implements Serializable {

        @Column(name = "board_id")
        private Integer boardId;

        @Column(name = "file_id")
        private Integer fileId;

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            BoardPhotoId that = (BoardPhotoId) obj;
            return boardId.equals(that.boardId) && fileId.equals(that.fileId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(boardId, fileId);
        }
    }
}
