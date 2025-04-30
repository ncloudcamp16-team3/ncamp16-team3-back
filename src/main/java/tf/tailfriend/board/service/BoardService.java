package tf.tailfriend.board.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tf.tailfriend.board.dto.BoardResponseDto;
import tf.tailfriend.board.dto.BoardStatusDto;
import tf.tailfriend.board.dto.CommentResponseDto;
import tf.tailfriend.board.entity.*;
import tf.tailfriend.board.exception.GetPostException;
import tf.tailfriend.board.repository.*;
import tf.tailfriend.file.entity.File;
import tf.tailfriend.global.service.StorageService;
import tf.tailfriend.user.entity.User;
import tf.tailfriend.user.exception.UserException;
import tf.tailfriend.user.repository.UserDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoardService {

    private final UserDao userDao;
    private final BoardDao boardDao;
    private final BoardTypeDao boardTypeDao;
    private final CommentDao commentDao;
    private final StorageService storageService;
    private final ProductDao productDao;
    private final BoardBookmarkDao boardBookmarkDao;
    private final BoardLikeDao boardLikeDao;

    @Transactional(readOnly = true)
    public Page<BoardResponseDto> getAllBoards(Pageable pageable) {
        Page<Board> boards = boardDao.findAll(pageable);
//        log.info("boards: {}", boards.getContent());
        return convertToDtoPage(boards);
    }

    @Transactional(readOnly = true)
    public Page<BoardResponseDto> getBoardsByType(Integer boardTypeId, Pageable pageable) {
        BoardType boardType = boardTypeDao.findById(boardTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Board type not found"));

        Page<Board> boards = boardDao.findByBoardTypeOrderByCreatedAtDesc(boardType, pageable);
        return convertToDtoPage(boards);
    }

    @Transactional(readOnly = true)
    public Page<BoardResponseDto> getBoardsByTypeAndKeyword(Integer boardTypeId, String keyword,Pageable pageable) {
        BoardType boardType = boardTypeDao.findById(boardTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Board type not found"));

        Page<Board> boards = boardDao.findByTitleContainingAndBoardTypeOrderByCreatedAtDesc(keyword, boardType, pageable);
        return convertToDtoPage(boards);
    }

    public BoardStatusDto getBoardStatus(Integer userId, Integer boardId) {
        boolean liked = boardLikeDao.findByIdUserIdAndIdBoardPostId(userId, boardId).isPresent();
        boolean bookmarked = boardBookmarkDao.findByIdUserIdAndIdBoardPostId(userId, boardId).isPresent();

        return BoardStatusDto.builder()
                .liked(liked)
                .bookmarked(bookmarked)
                .build();
    }

    @Transactional(readOnly = true)
    public BoardResponseDto getBoardById(Integer boardId) {
        Optional<Product> usedBoard = productDao.findById(boardId);

        BoardResponseDto boardResponseDto;
        if(usedBoard.isEmpty()) {
            Board board = boardDao.findById(boardId)
                    .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다: " + boardId));



            List<Comment> comments = commentDao.findByBoardIdAndParentIdIsNull(boardId);
            List<CommentResponseDto> commentDtos = comments.stream()
                    .map(CommentResponseDto::fromEntity)
                    .collect(Collectors.toList());

            setCommentImgPreSignUrl(commentDtos);

            boardResponseDto = BoardResponseDto.fromEntityWithComments(board, commentDtos);

        } else {
            boardResponseDto = BoardResponseDto.fromProductEntity(usedBoard.get());
        }

        // 모든 사진의 URL 생성
        List<String> imageUrls = boardResponseDto.getImageUrls().stream()
                .map(photo -> storageService.generatePresignedUrl(photo))
                .collect(Collectors.toList());

        boardResponseDto.setAuthorProfileImg(storageService.generatePresignedUrl(boardResponseDto.getAuthorProfileImg()));
        boardResponseDto.setImageUrls(imageUrls);

        log.info("\n게시판 응답Dto {}", boardResponseDto);

        return boardResponseDto;
    }

    private void setCommentImgPreSignUrl(List<CommentResponseDto> commentDtos) {
        for(CommentResponseDto commentDto: commentDtos) {
            commentDto.setAuthorProfileImg(storageService.generatePresignedUrl(commentDto.getAuthorProfileImg()));
            for(CommentResponseDto child: commentDto.getChildren()) {
                child.setAuthorProfileImg(storageService.generatePresignedUrl(child.getAuthorProfileImg()));
            }
        }
    }

    @Transactional
    public void bookmarkAdd(Integer userId, Integer BoardId) {
        User user = userDao.findById(userId).orElseThrow(() -> new UserException());
        Board board = boardDao.findById(BoardId).orElseThrow(() -> new GetPostException());
        BoardBookmark bookmarkEntity = BoardBookmark.of(board, user);

        boardBookmarkDao.save(bookmarkEntity);
    }

    @Transactional
    public void bookmarkDelete(Integer userId, Integer BoardId) {
        User user = userDao.findById(userId).orElseThrow(() -> new UserException());
        Board board = boardDao.findById(BoardId).orElseThrow(() -> new GetPostException());
        BoardBookmark bookmarkEntity = BoardBookmark.of(board, user);

        boardBookmarkDao.delete(bookmarkEntity);
    }

    @Transactional
    public void likeAdd(Integer userId, Integer BoardId) {
        User user = userDao.findById(userId).orElseThrow(() -> new UserException());
        Board board = boardDao.findById(BoardId).orElseThrow(() -> new GetPostException());
        BoardLike likeEntity = BoardLike.of(user, board);

        board.increaseLikeCount();

        log.info("\n\n\n 증가 후 좋아요 수 {}", board.getLikeCount());
        boardDao.save(board);
        boardLikeDao.save(likeEntity);
    }

    @Transactional
    public void likeDelete(Integer userId, Integer BoardId) {
        User user = userDao.findById(userId).orElseThrow(() -> new UserException());
        Board board = boardDao.findById(BoardId).orElseThrow(() -> new GetPostException());
        BoardLike likeEntity = BoardLike.of(user, board);

        board.decreaseLikeCount();
        
        log.info("\n\n\n 감소 후 좋아요 수 {}", board.getLikeCount());
        boardDao.save(board);
        boardLikeDao.delete(likeEntity);
    }

    @Transactional(readOnly = true)
    public Page<BoardResponseDto> searchBoards(String searchTerm, String searchField,
                                               Integer boardTypeId, Pageable pageable) {
        Page<Board> boards = null;

        switch (searchField) {
            case "title":
                if (boardTypeId != null) {
                    BoardType boardType = boardTypeDao.findById(boardTypeId)
                            .orElseThrow(() -> new IllegalArgumentException("Invalid board type ID: " + boardTypeId));
                    boards = boardDao.findByTitleContainingAndBoardTypeOrderByCreatedAtDesc(searchTerm, boardType, pageable);
                } else {
                    boards = boardDao.findByTitleContaining(searchTerm, pageable);
                }
                break;

            case "content":
                if (boardTypeId != null) {
                    BoardType boardType = boardTypeDao.findById(boardTypeId)
                            .orElseThrow(() -> new IllegalArgumentException("Invalid board type ID: " + boardTypeId));
                    boards = boardDao.findByContentContainingAndBoardType(searchTerm, boardType, pageable);
                } else {
                    boards = boardDao.findByContentContaining(searchTerm, pageable);
                }
                break;

            case "author":
                if (boardTypeId != null) {
                    BoardType boardType = boardTypeDao.findById(boardTypeId)
                            .orElseThrow(() -> new IllegalArgumentException("Invalid board type ID: " + boardTypeId));
                    boards = boardDao.findByUserNicknameContainingAndBoardType(searchTerm, boardType, pageable);
                } else {
                    boards = boardDao.findByUserNicknameContaining(searchTerm, pageable);
                }
                break;
        }

        return convertToDtoPage(boards);
    }

    @Transactional
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

    @Transactional
    public void deleteBoardById(Integer boardId) {
        Board board = boardDao.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 번호가 없습니다"));

        boardDao.delete(board);
    }

    // Board Entity를 BoardResponseDto로 변환하는 헬퍼 메서드
    private BoardResponseDto convertToDto(Board board) {
        // 기본 DTO 생성 (기존 fromEntity 메서드 활용)
        BoardResponseDto dto = BoardResponseDto.fromEntity(board);

        // 이미지 URL 처리
        if (board.getPhotos() != null && !board.getPhotos().isEmpty()) {
            List<String> imageUrls = new ArrayList<>();

            // 각 사진의 파일 경로를 URL로 변환
            for (BoardPhoto photo : board.getPhotos()) {
                String imageUrl = storageService.generatePresignedUrl(photo.getFile().getPath());
                imageUrls.add(imageUrl);
            }

            // 이미지 URL 목록 설정 (이 메서드는 firstImageUrl도 자동으로 설정함)
            dto.setImageUrls(imageUrls);
        }

        return dto;
    }

    // Board Entity Page를 BoardResponseDto Page로 변환하는 헬퍼 메서드
    private Page<BoardResponseDto> convertToDtoPage(Page<Board> boards) {
//        log.info("boards: {}", boards.getContent());
        return boards.map(this::convertToDto);
    }
}
