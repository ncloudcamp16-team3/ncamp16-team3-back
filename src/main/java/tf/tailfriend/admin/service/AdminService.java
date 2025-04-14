package tf.tailfriend.admin.service;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tf.tailfriend.admin.dto.AdminLoginResponse;
import tf.tailfriend.admin.entity.Admin;
import tf.tailfriend.admin.repository.AdminDao;
import tf.tailfriend.global.config.JwtTokenProvider;

import java.util.Collections;
import java.util.List;

@Service
public class AdminService {

    private final AdminDao adminDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AdminService(AdminDao adminDao, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.adminDao = adminDao;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public AdminLoginResponse login(String email, String password) {
        Admin admin = adminDao.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("이메일 오류"));

        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new IllegalArgumentException("비밀번호 오류");
        }

        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, null, authorities);

        String token = jwtTokenProvider.createToken(authentication);

        return AdminLoginResponse.builder()
                .token(token)
                .email(email)
                .build();
    }
}
