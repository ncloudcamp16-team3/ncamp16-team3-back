package tf.tailfriend.petsta.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tf.tailfriend.global.config.UserPrincipal;
import tf.tailfriend.global.service.StorageServiceException;
import tf.tailfriend.petsta.entity.dto.*;
import tf.tailfriend.petsta.service.PetstaPostService;
import tf.tailfriend.petsta.service.PetstaService;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/petsta/post")
@RequiredArgsConstructor
public class PetstaPostController {

    private final PetstaPostService petstaPostService;
    private final PetstaService petstaService;

    @PostMapping("/add/photo")
    public ResponseEntity<String> addPhoto(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(value = "content") String content,
            @RequestPart(value = "image") MultipartFile imageFile
    ) throws StorageServiceException {
        Integer userId = userPrincipal.getUserId();
        System.out.println(content);
        petstaPostService.uploadPhoto(userId,content,imageFile);
        return ResponseEntity.ok("업로드 성공");
    }


    @PostMapping("/add/video")
    public ResponseEntity<String> addVideo(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(value = "content") String content,
            @RequestParam(value = "trimStart") String trimStart,
            @RequestParam(value = "trimEnd") String trimEnd,
            @RequestPart(value = "video") MultipartFile videoFile
    ) throws StorageServiceException, IOException, InterruptedException {
        Integer userId = userPrincipal.getUserId();
        System.out.println(content);
        System.out.println(trimStart);
        System.out.println(trimEnd);
        petstaPostService.uploadVideo(userId,content,trimStart,trimEnd,videoFile);
        return ResponseEntity.ok("업로드 성공");
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<Void> updatePostContent(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Integer postId,
            @RequestBody PostUpdateDto dto
    ) throws AccessDeniedException {
        Integer userId = userPrincipal.getUserId();
        petstaPostService.updatePostContent(userId, postId, dto.getContent());
        return ResponseEntity.ok().build();
    }


    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Integer postId
    ) {
        petstaPostService.deletePost(userPrincipal.getUserId(), postId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @GetMapping("/lists")
    public ResponseEntity<PetstaMainPageResponseDto> getPostListsAndFollowings(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Integer userId = userPrincipal.getUserId();

        List<PetstaPostResponseDto> posts = petstaPostService.getAllPosts(userId, page, size);
        List<PetstaUpdatedUserDto> followings = page == 0
                ? petstaService.getTopFollowedUsers(userId)
                : Collections.emptyList();

        PetstaMainPageResponseDto response = new PetstaMainPageResponseDto(posts, followings);
        return ResponseEntity.ok(response);
    }



    @GetMapping("/{postId}")
    public ResponseEntity<PetstaPostResponseDto> getPostbyId(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("postId") Integer postId) {
        Integer userId = userPrincipal.getUserId();
        PetstaPostResponseDto post = petstaPostService.getPostById(userId,postId);
        return ResponseEntity.ok(post);
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<String> toggleLike(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("postId") Integer postId
    ) {
        Integer userId = userPrincipal.getUserId();
        petstaPostService.toggleLike(userId, postId);
        return ResponseEntity.ok("좋아요 토글 완료");
    }


    @PostMapping("/{postId}/bookmark")
    public ResponseEntity<String> toggleBookmark(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("postId") Integer postId
    ) {
        Integer userId = userPrincipal.getUserId();
        petstaPostService.toggleBookmark(userId, postId);
        return ResponseEntity.ok("북마크 토글 완료");
    }

    @PostMapping("/{postId}/add/comment")
    public ResponseEntity<PetstaCommentResponseDto> addComment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Integer postId,
            @RequestBody PetstaCommentRequestDto requestDto
    ) {
        PetstaCommentResponseDto responseDto = petstaPostService.addComment(
                postId,
                userPrincipal.getUserId(),
                requestDto.getContent(),
                requestDto.getParentId(),
                requestDto.getMention()
        );
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Integer commentId
    ) {
        petstaPostService.deleteComment(userPrincipal.getUserId(), commentId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }


    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<PetstaCommentResponseDto>> getParentComments(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Integer postId
    ) {
        int currentId = userPrincipal.getUserId();
        List<PetstaCommentResponseDto> parentComments = petstaPostService.getParentCommentsByPostId(currentId, postId);
        return ResponseEntity.ok(parentComments);
    }

    @GetMapping("/{commentId}/replies")
    public ResponseEntity<List<PetstaCommentResponseDto>> getReplyComments(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Integer commentId
    ) {
        int currentId = userPrincipal.getUserId();
        List<PetstaCommentResponseDto> parentComments = petstaPostService.getReplyCommentsByCommentId(commentId,currentId);
        return ResponseEntity.ok(parentComments);
    }

}
