package tf.tailfriend.board.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tf.tailfriend.board.dto.BoardResponseDto;
import tf.tailfriend.board.dto.CommentResponseDto;
import tf.tailfriend.board.entity.Board;
import tf.tailfriend.board.entity.BoardType;
import tf.tailfriend.board.entity.Comment;
import tf.tailfriend.board.repository.BoardDao;
import tf.tailfriend.board.repository.CommentDao;
import tf.tailfriend.file.entity.File;
import tf.tailfriend.global.service.StorageService;
import tf.tailfriend.user.entity.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoardService {

    private final BoardDao boardDao;
    private final CommentDao commentDao;
    private final StorageService storageService;

    @Transactional(readOnly = true)
    public Page<BoardResponseDto> getAllBoards(Pageable pageable) {
        Page<Board> boards = boardDao.findAll(pageable);

        return boards.map(board -> {
            BoardResponseDto dto = BoardResponseDto.fromEntity(board);

            if (!board.getPhotos().isEmpty()) {
                String imagePath = board.getPhotos().get(0).getFile().getPath();
                String imageUrl = storageService.generatePresignedUrl(imagePath);
                dto.updateFirstImageUrl(imageUrl);
            }

            return dto;
        });
    }

    @Transactional(readOnly = true)
    public Page<BoardResponseDto> getBoardsByType(Integer boardTypeId, Pageable pageable) {
        Page<Board> boards = boardDao.findByBoardTypeId(boardTypeId, pageable);

        return boards.map(board -> {
            BoardResponseDto dto = BoardResponseDto.fromEntity(board);

            // 첫 번째 사진이 있는 경우 URL 생성
            if (!board.getPhotos().isEmpty()) {
                String imagePath = board.getPhotos().get(0).getFile().getPath();
                String imageUrl = storageService.generatePresignedUrl(imagePath);
                dto.updateFirstImageUrl(imageUrl);
            }

            return dto;
        });
    }

    @Transactional(readOnly = true)
    public BoardResponseDto getBoardById(Integer boardId) {
        Board board = boardDao.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다: " + boardId));

        List<Comment> comments = commentDao.findByBoardId(boardId);
        List<CommentResponseDto> commentDtos = comments.stream()
                .map(CommentResponseDto::fromEntity)
                .collect(Collectors.toList());

        BoardResponseDto boardResponseDto = BoardResponseDto.fromEntityWithComments(board, commentDtos);

        // 모든 사진의 URL 생성
        List<String> imageUrls = board.getPhotos().stream()
                .map(photo -> storageService.generatePresignedUrl(photo.getFile().getPath()))
                .collect(Collectors.toList());

        boardResponseDto.setImageUrls(imageUrls);

        return boardResponseDto;
    }

    public Board createBoard(String title, String content, BoardType boardType, User user, List<File> files) {
        // 필수 파라미터 검증
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("게시글 제목은 필수입니다.");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("게시글 내용은 필수입니다.");
        }

        if (boardType == null) {
            throw new IllegalArgumentException("게시판 타입은 필수입니다.");
        }

        if (user == null) {
            throw new IllegalArgumentException("작성자 정보는 필수입니다.");
        }

        Board board = Board.builder()
                .title(title)
                .content(content)
                .boardType(boardType)
                .user(user)
                .build();

        if (files != null && !files.isEmpty()) {
            for (File file : files) {
                board.addPhoto(file);
            }
        }

        return boardDao.save(board);
    }
}
