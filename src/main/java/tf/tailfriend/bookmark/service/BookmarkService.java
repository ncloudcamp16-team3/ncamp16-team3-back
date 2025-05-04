package tf.tailfriend.bookmark.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tf.tailfriend.board.dto.BoardBookmarkResponseDto;
import tf.tailfriend.board.dto.BoardResponseDto;
import tf.tailfriend.board.entity.BoardBookmark;
import tf.tailfriend.board.repository.BoardBookmarkDao;
import tf.tailfriend.board.service.BoardService;
import tf.tailfriend.global.service.StorageService;
import tf.tailfriend.petsta.entity.PetstaBookmark;
import tf.tailfriend.petsta.entity.PetstaPost;
import tf.tailfriend.petsta.entity.dto.PetstaBookmarkResponseDto;
import tf.tailfriend.petsta.repository.PetstaBookmarkDao;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final PetstaBookmarkDao petstaBookmarkDao;
    private final BoardBookmarkDao boardBookmarkDao;
    private final StorageService storageService;
    private final BoardService boardService;

    //사용자의 펫스타 북마크 목록을 조회
    @Transactional(readOnly = true)
    public List<PetstaBookmarkResponseDto> getPetstaBookmarks(Integer userId) {
        List<PetstaBookmark> bookmarks = petstaBookmarkDao.findByUserIdAndNotDeleted(userId);

        return bookmarks.stream().map(bookmark -> {
            PetstaPost post = bookmark.getPetstaPost();

            // 게시물 파일 URL 생성 (동영상인 경우 썸네일 사용)
            String filePath = post.getThumbnailFile() != null
                    ? post.getThumbnailFile().getPath()
                    : post.getFile().getPath();
            String fileUrl = storageService.generatePresignedUrl(filePath);

            // 유저 프로필 URL 생성
            String userPhotoUrl = storageService.generatePresignedUrl(
                    post.getUser().getFile().getPath());

            return PetstaBookmarkResponseDto.fromBookmark(bookmark, fileUrl, userPhotoUrl);
        }).collect(Collectors.toList());
    }


    // 사용자의 게시글 북마크 목록을 조회
    @Transactional(readOnly = true)
    public List<BoardBookmarkResponseDto> getBoardBookmarks(Integer userId) {
        List<BoardBookmark> bookmarks = boardBookmarkDao.findByUserId(userId);

        return bookmarks.stream().map(bookmark -> {
            // 게시글 이미지 URL 목록 생성
            List<String> imageUrls = bookmark.getBoard().getPhotos().stream()
                    .map(photo -> storageService.generatePresignedUrl(photo.getFile().getPath()))
                    .collect(Collectors.toList());

            return BoardBookmarkResponseDto.fromBookmark(bookmark, imageUrls);
        }).collect(Collectors.toList());
    }

    //특정 게시판 타입에 해당하는 사용자의 게시글 북마크 목록을 조회
    @Transactional(readOnly = true)
    public List<BoardBookmarkResponseDto> getBoardBookmarksByBoardType(Integer userId, Integer boardTypeId) {
        List<BoardBookmark> bookmarks;

        if (boardTypeId != null && boardTypeId > 0) {
            bookmarks = boardBookmarkDao.findByUserIdAndBoardBoardTypeId(userId, boardTypeId);
        } else {
            bookmarks = boardBookmarkDao.findByUserId(userId);
        }

        return bookmarks.stream().map(bookmark -> {
            // 게시글 이미지 URL 목록 생성
            List<String> imageUrls = bookmark.getBoard().getPhotos().stream()
                    .map(photo -> storageService.generatePresignedUrl(photo.getFile().getPath()))
                    .collect(Collectors.toList());

            return BoardBookmarkResponseDto.fromBookmark(bookmark, imageUrls);
        }).collect(Collectors.toList());
    }


    // 사용자가 작성한 게시글 목록을 페이징 처리하여 조회
    @Transactional(readOnly = true)
    public Page<BoardResponseDto> getUserPostsPaged(Integer userId, Pageable pageable) {
        return boardService.getUserBoardsPaged(userId, pageable);
    }
}