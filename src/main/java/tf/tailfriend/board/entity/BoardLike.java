package tf.tailfriend.board.entity;

import jakarta.persistence.*;
import lombok.*;
import tf.tailfriend.user.entity.User;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "board_likes")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardLike {

    @EmbeddedId
    private BoardLikeId id;

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
    public static class BoardLikeId implements Serializable {

        @Column(name = "board_post_id")
        private Integer boardPostId;

        @Column(name = "user_id")
        private Integer userId;

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            BoardLikeId that = (BoardLikeId) obj;
            return boardPostId.equals(that.boardPostId) && userId.equals(that.userId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(boardPostId, userId);
        }
    }

    public static BoardLike of(User user, Board board) {
        BoardLikeId id = BoardLikeId.builder()
                .userId(user.getId())
                .boardPostId(board.getId())
                .build();

        return BoardLike.builder()
                .id(id)
                .user(user)
                .board(board)
                .build();
    }
}
