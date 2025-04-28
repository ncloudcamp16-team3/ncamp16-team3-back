package tf.tailfriend.petsta.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tf.tailfriend.global.config.UserPrincipal;
import tf.tailfriend.global.service.RedisService;
import tf.tailfriend.petsta.entity.dto.PetstaFollowingUserDto;
import tf.tailfriend.petsta.entity.dto.PetstaUserpageResponseDto;
import tf.tailfriend.petsta.service.PetstaService;
import tf.tailfriend.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/petsta")
@RequiredArgsConstructor
public class PetstaController {

    private final PetstaService petstaService;
    private final UserService userService;
    private final RedisService redisService;

    @Value("${URL}")
    private String mainUrl;

    @GetMapping("/hello")
    public String hello() {
        System.out.println(mainUrl);
        return "hello";
    }

    @GetMapping("/users/{userId}/page")
    public ResponseEntity<PetstaUserpageResponseDto> getUserPage(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Integer userId
    ){
        int currentId = userPrincipal.getUserId();
        return ResponseEntity.ok(petstaService.getUserPage(currentId, userId));
    }


    @GetMapping("/users/{userId}/name")
    public ResponseEntity<String> getName(
            @PathVariable Integer userId
    ){
        return ResponseEntity.ok(userService.getUsername(userId));
    }

    @GetMapping("/users/{userId}/followers")
    public ResponseEntity<List<PetstaFollowingUserDto>> getFollowers(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        int currentId = userPrincipal.getUserId();
        return ResponseEntity.ok(
                petstaService.getFollowersWithFollowingStatus(userId, currentId, page, size)
        );
    }

    @GetMapping("/users/{userId}/followings")
    public ResponseEntity<List<PetstaFollowingUserDto>> getFollowings(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        int currentId = userPrincipal.getUserId();
        return ResponseEntity.ok(
                petstaService.getFollowingsWithFollowingStatus(userId, currentId, page, size)
        );
    }


}
