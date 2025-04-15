package tf.tailfriend.admin.controller;//package tf.tailfriend.admin.controller;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import tf.tailfriend.admin.dto.AdminLoginRequest;
//import tf.tailfriend.admin.dto.AdminLoginResponse;
//import tf.tailfriend.admin.service.AdminService;
//
//@RestController
//@RequestMapping("/admin")
//@RequiredArgsConstructor
//public class AdminController {
//
//    private final AdminService adminService;
//
//    @PostMapping("/login")
//    public ResponseEntity<AdminLoginResponse> login(@RequestBody AdminLoginRequest request) {
//        AdminLoginResponse response = adminService.login(request.getEmail(), request.getPassword());
//        return ResponseEntity.ok(response);
//    }
//}
