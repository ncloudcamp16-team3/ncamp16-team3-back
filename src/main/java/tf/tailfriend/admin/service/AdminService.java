package tf.tailfriend.admin.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tf.tailfriend.admin.dto.AdminLoginResponse;
import tf.tailfriend.admin.entity.Admin;
import tf.tailfriend.admin.exception.EmailException;
import tf.tailfriend.admin.exception.ExistEmailException;
import tf.tailfriend.admin.exception.PasswordException;
import tf.tailfriend.admin.repository.AdminDao;
import tf.tailfriend.global.config.JwtTokenProvider;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class AdminService {

    private final AdminDao adminDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    private final Set<String> tokenBlackList = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public AdminService(AdminDao adminDao, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.adminDao = adminDao;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public Admin register(String email, String password) {
        if (adminDao.findByEmail(email).isPresent()) {
            throw new ExistEmailException();
        }

        String encodedPassword = passwordEncoder.encode(password);

        Admin admin = Admin.builder()
                .email(email)
                .password(encodedPassword)
                .build();

        return adminDao.save(admin);
    }

    @Transactional
    public AdminLoginResponse login(String email, String password) {
        Admin admin = adminDao.findByEmail(email)
                .orElseThrow(() -> new EmailException());

        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new PasswordException();
        }

        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, null, authorities);

        String token = jwtTokenProvider.createToken(authentication);

        return AdminLoginResponse.builder()
                .token(token)
                .email(email)
                .build();
    }

    public void logout(String token) {
        if (token != null && !token.isEmpty()) {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            tokenBlackList.add(token);
        }
    }

    public boolean isTokenBlacklisted(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return tokenBlackList.contains(token);
    }
}
