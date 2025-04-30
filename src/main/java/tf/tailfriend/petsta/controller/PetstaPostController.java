package tf.tailfriend.petsta.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tf.tailfriend.board.entity.Board;
import tf.tailfriend.board.entity.Comment;
import tf.tailfriend.global.config.UserPrincipal;
import tf.tailfriend.global.service.StorageServiceException;
import tf.tailfriend.notification.scheduler.NotificationScheduler;
import tf.tailfriend.petsta.entity.PetstaComment;
import tf.tailfriend.petsta.entity.PetstaPost;
import tf.tailfriend.petsta.entity.dto.*;
import tf.tailfriend.petsta.repository.PetstaPostDao;
import tf.tailfriend.petsta.service.PetstaPostService;
import tf.tailfriend.petsta.service.PetstaService;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/petsta/post")
@RequiredArgsConstructor
public class PetstaPostController {

    private final PetstaPostService petstaPostService;
    private final PetstaService petstaService;
    private final PetstaPostDao petstaPostDao;
    private final NotificationScheduler notificationScheduler;

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
        System.out.println(post);
        System.out.println("너왜출력을안하냐?");
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
    public ResponseEntity<?> addComment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Integer postId,
            @RequestBody PetstaCommentRequestDto requestDto
    ) {

            //펫스타 댓글 알림 위해 주석

//        petstaPostService.addComment(
//                postId,
//                userPrincipal.getUserId(),
//                requestDto.getContent(),
//                requestDto.getParentId()
//        );

        // 펫스타 댓글 알림
        PetstaComment petstaComment=petstaPostService.addCommententity(
                postId,
                userPrincipal.getUserId(),
                requestDto.getContent(),
                requestDto.getParentId()
        );

        // 게시글 및 작성자 정보 조회
        PetstaPost petstaPost = petstaPostDao.getPetstaPostById(postId);
        Integer postOwnerId = petstaPost.getUser().getId();
        Integer commentWriterId = petstaComment.getUser().getId();

        // 부모 댓글 작성자 ID 추출
        Integer parentCommentWriterId = null;
        if (petstaComment.getParent() != null) {
            parentCommentWriterId = petstaComment.getParent().getUser().getId();
        }

        // 알림 대상 유저 식별
        Set<Integer> targetUserIds = new HashSet<>();
        if (!postOwnerId.equals(commentWriterId)) {
            targetUserIds.add(postOwnerId);
        }
        if (parentCommentWriterId != null && !parentCommentWriterId.equals(commentWriterId)) {
            targetUserIds.add(parentCommentWriterId);
        }

        System.out.println("✅ 알림 대상 유저 ID 목록: " + targetUserIds);

        for (Integer userId : targetUserIds) {
            notificationScheduler.sendNotificationAndSaveLog(
                    userId,
                    2, // 댓글 알림 타입
                    String.valueOf(petstaComment.getId()),
                    petstaComment.getCreatedAt(),
                    "💬 펫스타 댓글 알림 전송 완료: 작성 유저 닉네임={}, 댓글내용={}",
                    petstaComment.getUser().getNickname(),
                    petstaComment.getContent(),
                    "❌ 펫스타 댓글 알림 전송 실패: commentId=" + petstaComment.getId()
            );
        }

        return ResponseEntity.ok("댓글이 성공적으로 작성되었습니다.");
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
        List<PetstaCommentResponseDto> parentComments = petstaPostService.getReplyCommentsByCommentId(currentId,commentId);
        return ResponseEntity.ok(parentComments);
    }

}
