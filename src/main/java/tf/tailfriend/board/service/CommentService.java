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
    public Comment addComment(String content, Integer boardId, Integer userId, Integer refCommentId) {
        if(content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("댓글이 공백일 수 없습니다");
        }
        Board board = boardDao.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("Board not found"));

        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));



        Comment.CommentBuilder builder = Comment.builder()
                .user(user)
                .board(board)
                .content(content);

        if (refCommentId != null) {
            Comment refComment = commentDao.findById(refCommentId)
                    .orElseThrow(() -> new IllegalArgumentException("comment not found"));

            if (refComment.getParent() == null) {
                builder.parent(refComment)
                        .refComment(refComment); // 직접 답글
            } else {
                builder.parent(refComment.getParent())
                        .refComment(refComment); // 대댓글
            }
        }

        Comment comment = builder.build();

        Comment savedComment = commentDao.save(comment);

        board.increaseCommentCount();
        boardDao.save(board);

        return savedComment;
    }

    @Transactional
    public void updateComment(String content, Integer commentId) {
        Comment comment = commentDao.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("comment not found"));

        comment.updateContent(content);

        commentDao.save(comment);
    }

    @Transactional
    public void deleteComment(Integer commentId) {
        Comment comment = commentDao.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("comment not found"));

        comment.setDeleted();

        Board board = comment.getBoard();
        board.decreaseCommentCount();
        boardDao.save(board);

        commentDao.save(comment);
    }
}
