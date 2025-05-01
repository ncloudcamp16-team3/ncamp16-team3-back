package tf.tailfriend.bookmark.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tf.tailfriend.board.dto.BoardBookmarkResponseDto;
import tf.tailfriend.board.entity.BoardBookmark;
import tf.tailfriend.board.repository.BoardBookmarkDao;
import tf.tailfriend.global.service.StorageService;
import tf.tailfriend.petsta.entity.PetstaBookmark;
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

    //사용자의 펫스타 북마크 목록을 조회
    @Transactional(readOnly = true)
    public List<PetstaBookmarkResponseDto> getPetstaBookmarks(Integer userId) {
        List<PetstaBookmark> bookmarks = petstaBookmarkDao.findByUserId(userId);

        return bookmarks.stream().map(bookmark -> {
            // 게시물 파일 URL 생성
            String fileUrl = storageService.generatePresignedUrl(
                    bookmark.getPetstaPost().getFile().getPath());

            // 유저 프로필 URL 생성
            String userPhotoUrl = storageService.generatePresignedUrl(
                    bookmark.getPetstaPost().getUser().getFile().getPath());

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
}