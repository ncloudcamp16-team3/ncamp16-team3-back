package tf.tailfriend.board.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tf.tailfriend.board.entity.Board;
import tf.tailfriend.board.entity.Comment;
import tf.tailfriend.board.repository.BoardDao;
import tf.tailfriend.board.repository.CommentDao;
import tf.tailfriend.user.entity.User;
import tf.tailfriend.user.repository.UserDao;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final BoardDao boardDao;
    private final CommentDao commentDao;
    private final UserDao userDao;

    @Transactional
    public Comment addComment(Integer boardId, String content, Integer userId) {
        Board board = boardDao.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("Board not found"));

        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Comment comment = Comment.builder()
                .user(user)
                .board(board)
                .content(content)
                .build();

        Comment savedComment = commentDao.save(comment);

        board.increaseCommentCount();
        boardDao.save(board);

        return savedComment;
    }
}
