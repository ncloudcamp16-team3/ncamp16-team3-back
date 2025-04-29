package tf.tailfriend.board.entity;

import jakarta.persistence.*;
import lombok.*;
import tf.tailfriend.user.entity.User;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "board_bookmarks")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardBookmark {

    @EmbeddedId
    private BoardBookmarkId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @MapsId("boardPostId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_post_id")
    private Board board;

    @Embeddable
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BoardBookmarkId implements Serializable {

        @Column(name = "user_id")
        private Integer userId;

        @Column(name = "board_post_id")
        private Integer boardPostId;

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            BoardBookmarkId that = (BoardBookmarkId) obj;
            return userId.equals(that.userId) && boardPostId.equals(that.boardPostId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, boardPostId);
        }
    }

    public static BoardBookmark of(Board board, User user) {
        BoardBookmarkId id = BoardBookmarkId.builder()
                .userId(user.getId())
                .boardPostId(board.getId())
                .build();

        return BoardBookmark.builder()
                .id(id)
                .user(user)
                .board(board)
                .build();
    }
}
