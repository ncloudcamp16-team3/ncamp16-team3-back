package tf.tailfriend.board.entity;

import jakarta.persistence.*;
import lombok.*;
import tf.tailfriend.file.entity.File;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "board_photos")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"board", "file"})
public class BoardPhoto {

    @EmbeddedId
    private BoardPhotoId id;

    @MapsId("boardId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @MapsId("fileId")
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "file_id")
    private File file;

    @Embeddable
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
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

    public static BoardPhoto of(Board board, File file) {
        BoardPhotoId id = BoardPhotoId.builder()
                .boardId(board.getId())
                .fileId(file.getId())
                .build();

        return BoardPhoto.builder()
                .id(id)
                .board(board)
                .file(file)
                .build();
    }
}
