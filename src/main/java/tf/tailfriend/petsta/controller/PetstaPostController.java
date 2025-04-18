package tf.tailfriend.petsta.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tf.tailfriend.global.config.UserPrincipal;
import tf.tailfriend.global.service.StorageServiceException;
import tf.tailfriend.petsta.entity.dto.PostResponseDto;
import tf.tailfriend.petsta.service.PetstaPostService;
import tf.tailfriend.file.entity.File;

import java.util.List;

@RestController
@RequestMapping("/api/petsta/post")
@RequiredArgsConstructor
public class PetstaPostController {

    private final PetstaPostService petstaPostService;

    @PostMapping("/add/photo")
    public ResponseEntity<String> addPhoto(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(value = "content") String content,
            @RequestPart(value = "image") MultipartFile imageFile
    ) throws StorageServiceException {
        Integer userId = userPrincipal.getUserId();
        System.out.println(content);
        petstaPostService.uploadPost(userId,content,imageFile);
        return ResponseEntity.ok("업로드 성공");
    }

    @GetMapping("/lists")
    public ResponseEntity<List<PostResponseDto>> getPostLists() {
        List<PostResponseDto> posts = petstaPostService.getAllPosts();
        System.out.println(posts);
        return ResponseEntity.ok(posts);
    }

}
