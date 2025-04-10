package tf.tailfriend.board.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tf.tailfriend.user.entity.User;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "board_bookmarks")
@Getter
@Setter
@NoArgsConstructor
public class BoardBookmark {

    @EmbeddedId
    private BoardBookmarkId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @MapsId("boardId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    public static class BoardBookmarkId implements Serializable {

        @Column(name = "user_id")
        private Integer userId;

        @Column(name = "board_id")
        private Integer boardId;

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            BoardBookmarkId that = (BoardBookmarkId) obj;
            return userId.equals(that.userId) && boardId.equals(that.boardId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, boardId);
        }
    }
}
