package tf.tailfriend.petsta.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/petsta")
public class PetstaController {

    @Value("${URL}")
    private String mainUrl;

    @GetMapping("/hello")
    public String hello() {
        System.out.println(mainUrl);
        return "hello";
    }
}
