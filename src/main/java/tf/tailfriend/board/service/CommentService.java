package tf.tailfriend.board.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tf.tailfriend.board.dto.CommentResponseDto;
import tf.tailfriend.board.entity.Board;
import tf.tailfriend.board.entity.Comment;
import tf.tailfriend.board.repository.BoardDao;
import tf.tailfriend.board.repository.CommentDao;
import tf.tailfriend.global.service.StorageService;
import tf.tailfriend.user.entity.User;
import tf.tailfriend.user.repository.UserDao;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final BoardDao boardDao;
    private final CommentDao commentDao;
    private final UserDao userDao;
    private final StorageService storageService;

    @Transactional
    public List<CommentResponseDto> getComments(Integer boardId) {
        List<Comment> comments = commentDao.findByBoardIdAndParentIdIsNull(boardId);

        List<CommentResponseDto> commentDtos = comments.stream()
                .map(CommentResponseDto::fromEntity)
                .collect(Collectors.toList());

        setCommentImgPreSignUrl(commentDtos);

        return commentDtos;
    }

    private void setCommentImgPreSignUrl(List<CommentResponseDto> commentDtos) {
        for (CommentResponseDto commentDto : commentDtos) {
            commentDto.setAuthorProfileImg(storageService.generatePresignedUrl(commentDto.getAuthorProfileImg()));
            for (CommentResponseDto child : commentDto.getChildren()) {
                child.setAuthorProfileImg(storageService.generatePresignedUrl(child.getAuthorProfileImg()));
            }
        }
    }

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
        if(content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("댓글이 공백일 수 없습니다");
        }

        Comment comment = commentDao.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("comment not found"));

        comment.updateContent(content);

        commentDao.save(comment);
    }

    @Transactional
    public Comment deleteComment(Integer commentId) {
        Comment comment = commentDao.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("comment not found"));

        comment.setDeleted();

        Board board = comment.getBoard();
        board.decreaseCommentCount();
        boardDao.save(board);

        return commentDao.save(comment);
    }
}
