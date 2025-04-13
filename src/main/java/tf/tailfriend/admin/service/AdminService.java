package tf.tailfriend.admin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tf.tailfriend.admin.entity.Admin;
import tf.tailfriend.admin.repository.AdminDao;

import java.util.Optional;

@Service
public class AdminService {

    private final AdminDao adminDao;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminService(AdminDao adminDao, PasswordEncoder passwordEncoder) {
        this.adminDao = adminDao;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 관리자 계정 생성
     * @param email 이메일
     * @param password 비밀번호
     * @return 생성된 관리자 객체
     * @throws IllegalArgumentException 이메일이 이미 존재하는 경우
     */
    @Transactional
    public Admin createAdmin(String email, String password) {
        // 이메일 중복 체크
        if (adminDao.existsByEmail(email)) {
            throw new IllegalArgumentException("Email: " + email + "는 이미 존재하는 이메일 입니다");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(password);

        // 관리자 객체 생성
        Admin admin = new Admin();
        admin.setEmail(email);
        admin.setPassword(encodedPassword);

        return adminDao.save(admin);
    }

    /**
     * 이메일로 관리자 정보 조회
     * @param email 이메일
     * @return 관리자 Optional 객체
     */
    @Transactional(readOnly = true)
    public Optional<Admin> findAdminByEmail(String email) {
        return adminDao.findByEmail(email);
    }

    /**
     * 로그인 검증
     * @param email 이메일
     * @param password 비밀번호
     * @return 로그인 성공 여부
     */
    @Transactional(readOnly = true)
    public boolean validateLogin(String email, String password) {
        Optional<Admin> adminOpt = findAdminByEmail(email);

        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            return passwordEncoder.matches(password, admin.getPassword());
        }

        return false;
    }
}
