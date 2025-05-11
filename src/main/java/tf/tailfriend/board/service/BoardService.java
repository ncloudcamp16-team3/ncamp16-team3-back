package tf.tailfriend.board.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tf.tailfriend.board.dto.BoardRequestDto;
import tf.tailfriend.board.dto.BoardResponseDto;
import tf.tailfriend.board.dto.BoardStatusDto;
import tf.tailfriend.board.dto.CommentResponseDto;
import tf.tailfriend.board.entity.*;
import tf.tailfriend.board.exception.GetBoardTypeException;
import tf.tailfriend.board.exception.GetPostException;
import tf.tailfriend.board.repository.*;
import tf.tailfriend.chat.repository.TradeMatchDao;
import tf.tailfriend.file.entity.File;
import tf.tailfriend.file.service.FileService;
import tf.tailfriend.global.exception.CustomException;
import tf.tailfriend.global.service.StorageService;
import tf.tailfriend.global.service.StorageServiceException;
import tf.tailfriend.user.entity.User;
import tf.tailfriend.user.exception.UnauthorizedException;
import tf.tailfriend.user.exception.UserException;
import tf.tailfriend.user.repository.UserDao;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private final FileService fileService;
    private final TradeMatchDao tradeMatchDao;

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
    public Page<BoardResponseDto> getBoardsByTypeAndKeyword(Integer boardTypeId, String keyword, Pageable pageable) {
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
        Optional<Product> productBoard = productDao.findById(boardId);

        BoardResponseDto boardResponseDto;
        if (productBoard.isEmpty()) {
            Board board = boardDao.findById(boardId)
                    .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다: " + boardId));

            List<Comment> comments = commentDao.findByBoardIdAndParentIdIsNull(boardId);

            List<CommentResponseDto> commentDtos = comments.stream()
                    .map(CommentResponseDto::fromEntity)
                    .collect(Collectors.toList());

            setCommentImgPreSignUrl(commentDtos);

            boardResponseDto = BoardResponseDto.fromEntityWithComments(board, commentDtos);

        } else {
            boardResponseDto = BoardResponseDto.fromProductEntity(productBoard.get());
        }

        List<String> imageUrls = boardResponseDto.getImageUrls().stream()
                .map(storageService::generatePresignedUrl)
                .collect(Collectors.toList());

        for (BoardResponseDto.PhotoDto photoDto : boardResponseDto.getPhotos()) {
            photoDto.setPath(storageService.generatePresignedUrl(photoDto.getPath()));
        }

        boardResponseDto.setAuthorProfileImg(storageService.generatePresignedUrl(boardResponseDto.getAuthorProfileImg()));
        boardResponseDto.setImageUrls(imageUrls);

        log.info("\n게시판 응답Dto {}", boardResponseDto);

        return boardResponseDto;
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

    @Transactional
    public Integer saveBoard(BoardRequestDto boardRequestDto, List<MultipartFile> photos, Integer userId) throws StorageServiceException {
        Integer boardTypeId = boardRequestDto.getBoardTypeId();

        String title = boardRequestDto.getTitle();
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("제목이 공백일 수 없습니다");
        }

        String content = boardRequestDto.getContent();
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("본문이 공백일 수 없습니다");
        }

        BoardType boardType = boardTypeDao.findById(boardTypeId)
                .orElseThrow(() -> new GetBoardTypeException());

        if (boardTypeId.equals(2)) {
            return saveUsedMarket(boardRequestDto, photos, userId, boardType);
        } else {
            return saveBoardTable(boardRequestDto, photos, userId, boardType);
        }

    }

    private Integer saveUsedMarket(BoardRequestDto boardRequestDto, List<MultipartFile> photos, Integer userId, BoardType boardType) throws StorageServiceException {
        Integer postId = boardRequestDto.getId();

        Product saveEntity;
        Integer savedBoardId = saveBoardTable(boardRequestDto, photos, userId, boardType);
        Board savedBoard = boardDao.findById(savedBoardId)
                .orElseThrow(() -> new GetPostException());
        if (postId == null) {

            saveEntity = Product.builder()
                    .board(savedBoard)
                    .price(boardRequestDto.getPrice())
                    .sell(true)
                    .address(boardRequestDto.getAddress())
                    .build();

            productDao.save(saveEntity);

            return savedBoardId;
        } else {
            if(!userId.equals(boardRequestDto.getAuthorId())) throw new UnauthorizedException();

            saveEntity = productDao.findById(postId)
                    .orElseThrow(() -> new GetPostException());

            saveEntity.updateProduct(boardRequestDto.getPrice(), boardRequestDto.getSell(), boardRequestDto.getAddress());

            productDao.save(saveEntity);

            return postId;
        }
    }

    private Integer saveBoardTable(BoardRequestDto boardRequestDto, List<MultipartFile> photos, Integer userId, BoardType boardType) throws StorageServiceException {
        Integer postId = boardRequestDto.getId();

        User user = userDao.findById(userId)
                .orElseThrow(() -> new UserException());

        Board saveEntity;
        if (postId == null) {
            saveEntity = Board.builder()
                    .boardType(boardType)
                    .title(boardRequestDto.getTitle())
                    .content(boardRequestDto.getContent())
                    .user(user)
                    .build();

            Board tempSaved = boardDao.save(saveEntity);

            if (photos != null) {
                List<File> imgs = fileSave(photos);
                for (File img : imgs) {
                    tempSaved.addPhoto(img);
                }
            }

            Board savedBoard = boardDao.save(tempSaved);

            return savedBoard.getId();
        } else {
            if (!userId.equals(boardRequestDto.getAuthorId())) throw new UnauthorizedException();
            Board updateTarget = boardDao.findById(postId)
                    .orElseThrow(() -> new GetPostException());

            List<Integer> deleteFileIds = boardRequestDto.getDeleteFileIds();
            for (Integer fileid : deleteFileIds) {
                File deletedFile = updateTarget.removePhotoByFileId(fileid);
                if (deletedFile != null) {
                    storageService.delete(deletedFile.getPath());
                }
            }

            if (photos != null) {
                List<File> imgs = fileSave(photos);
                for (File img : imgs) {
                    updateTarget.addPhoto(img);
                }
            }
            updateTarget.updateBoard(boardRequestDto.getTitle(), boardRequestDto.getContent());

            boardDao.save(updateTarget);

            return postId;
        }
    }

    private List<File> fileSave(List<MultipartFile> photos) {
        List<File> postImgs = new ArrayList<>();

        for (MultipartFile photo : photos) {

            File.FileType fileType = photo.getContentType().startsWith("image/")
                    ? File.FileType.PHOTO
                    : File.FileType.VIDEO;

            File savedFile = fileService.save(photo.getOriginalFilename(), "board", fileType);
            postImgs.add(savedFile);

            try (InputStream inputStream = photo.getInputStream()) {
                storageService.openUpload(savedFile.getPath(), inputStream);
            } catch (StorageServiceException | IOException e) {
                throw new CustomException() {
                    @Override
                    public HttpStatus getStatus() {
                        return HttpStatus.INTERNAL_SERVER_ERROR;
                    }

                    @Override
                    public String getMessage() {
                        return "Storage 파일 업로드에 실패하였습니다";
                    }
                };
            }
        }

        return postImgs;
    }

    @Transactional
    public void deleteBoard(Integer postId, Integer userId) throws StorageServiceException {
        Board deleteEntity = boardDao.findById(postId)
                .orElseThrow(() -> new GetPostException());

        if (!deleteEntity.getUser().getId().equals(userId)) {
            throw new UnauthorizedException();
        }

        boardLikeDao.deleteAllByBoard(deleteEntity);
        boardBookmarkDao.deleteAllByBoard(deleteEntity);
        commentDao.deleteAllByBoard(deleteEntity);

        if(deleteEntity.getBoardType().getId().equals(2)){
            Product deleteProduct = productDao.findById(postId).orElseThrow(() -> new CustomException() {
                @Override
                public HttpStatus getStatus() {
                    return HttpStatus.INTERNAL_SERVER_ERROR;
                }

                @Override
                public String getMessage() {
                    return "중고 상품을 찾지 못했습니다";
                }
            });
            productDao.delete(deleteProduct);
        }

        for (BoardPhoto boardPhoto: deleteEntity.getPhotos()) {
            storageService.delete(boardPhoto.getFile().getPath());
        }

        boardDao.delete(deleteEntity);
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

        tradeMatchDao.deleteAllByPostId(boardId);
        commentDao.deleteAllByBoard(board);
        boardLikeDao.deleteAllByBoard(board);
        boardBookmarkDao.deleteAllByBoard(board);
        boardDao.delete(board);
    }

    @Transactional(readOnly = true)
    public List<BoardResponseDto> getUserBoards(Integer userId) {
        List<Board> boards = boardDao.findByUserIdOrderByCreatedAtDesc(userId);

        return boards.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<BoardResponseDto> getUserBoardsPaged(Integer userId, Pageable pageable) {
        Page<Board> boards = boardDao.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return convertToDtoPage(boards);
    }

    // Board Entity를 BoardResponseDto로 변환하는 헬퍼 메서드
    private BoardResponseDto convertToDto(Board board) {
        BoardResponseDto dto;

        // boardType이 2(중고거래)이고 Product가 있는 경우
        if (board.getBoardType().getId().equals(2) && board.getProduct().isPresent()) {
            dto = BoardResponseDto.fromProductEntity(board.getProduct().get());
        } else {
            // 일반 게시판인 경우
            dto = BoardResponseDto.fromEntity(board);
        }

        // 공통 이미지 URL 처리
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

    public void setCompleteSell(Integer postId, Integer userId) {
        Product product = productDao.findById(postId).orElseThrow(() -> new IllegalArgumentException("게시물 조회에 실패하였습니다"));
        if (!Objects.equals(userId, product.getBoard().getUser().getId())) {
            throw new IllegalArgumentException("게시물에 대한 수정 권한이 없습니다");
        }

        product.updateProduct(product.getPrice(), false, product.getAddress());

        productDao.save(product);
    }
}
