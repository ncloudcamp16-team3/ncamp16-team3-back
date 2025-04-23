package tf.tailfriend.petsta.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tf.tailfriend.global.config.UserPrincipal;
import tf.tailfriend.petsta.service.PetstaService;

@RestController
@RequestMapping("/api/petsta")
@RequiredArgsConstructor
public class PetstaController {

    private final PetstaService petstaService;

    @Value("${URL}")
    private String mainUrl;

    @GetMapping("/hello")
    public String hello() {
        System.out.println(mainUrl);
        return "hello";
    }



}
