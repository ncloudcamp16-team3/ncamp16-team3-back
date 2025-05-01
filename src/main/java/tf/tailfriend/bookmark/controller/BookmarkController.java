package tf.tailfriend.bookmark.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tf.tailfriend.board.dto.BoardBookmarkResponseDto;
import tf.tailfriend.bookmark.service.BookmarkService;
import tf.tailfriend.global.config.UserPrincipal;
import tf.tailfriend.global.response.CustomResponse;

import tf.tailfriend.petsta.entity.dto.PetstaBookmarkResponseDto;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    // 사용자의 펫스타 북마크 목록을 조회
    @GetMapping("/petsta")
    public ResponseEntity<CustomResponse> getPetstaBookmarks(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Integer userId = userPrincipal.getUserId();
        List<PetstaBookmarkResponseDto> bookmarks = bookmarkService.getPetstaBookmarks(userId);

        return ResponseEntity.ok(new CustomResponse("펫스타 북마크 조회 성공", bookmarks));
    }

    // 사용자의 게시글 북마크 목록을 조회
    @GetMapping("/posts")
    public ResponseEntity<CustomResponse> getBoardBookmarks(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) Integer boardTypeId
    ) {
        Integer userId = userPrincipal.getUserId();
        List<BoardBookmarkResponseDto> bookmarks;

        if (boardTypeId != null && boardTypeId > 0) {
            bookmarks = bookmarkService.getBoardBookmarksByBoardType(userId, boardTypeId);
        } else {
            bookmarks = bookmarkService.getBoardBookmarks(userId);
        }

        return ResponseEntity.ok(new CustomResponse("게시글 북마크 조회 성공", bookmarks));
    }

    //사용자의 전체 북마크 목록을 조회
    @GetMapping
    public ResponseEntity<CustomResponse> getAllBookmarks(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Integer userId = userPrincipal.getUserId();

        List<PetstaBookmarkResponseDto> petstaBookmarks = bookmarkService.getPetstaBookmarks(userId);
        List<BoardBookmarkResponseDto> boardBookmarks = bookmarkService.getBoardBookmarks(userId);

        Map<String, Object> response = Map.of(
                "petstaBookmarks", petstaBookmarks,
                "boardBookmarks", boardBookmarks
        );

        return ResponseEntity.ok(new CustomResponse("북마크 목록 조회 성공", response));
    }
}