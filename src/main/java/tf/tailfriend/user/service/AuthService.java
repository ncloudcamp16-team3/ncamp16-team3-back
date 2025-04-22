package tf.tailfriend.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tf.tailfriend.global.config.JwtTokenProvider;
import tf.tailfriend.user.entity.User;
import tf.tailfriend.user.entity.dto.LoginRequestDto;
import tf.tailfriend.user.repository.UserDao;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserDao userDao;
    private final JwtTokenProvider jwtTokenProvider;

    public String login(LoginRequestDto dto) {
        String snsAccountId = dto.getSnsAccountId();

        User user = userDao.findBySnsAccountId(snsAccountId)
                .orElseThrow(() -> new IllegalArgumentException("❌ 존재하지 않는 계정입니다: " + snsAccountId));

        return jwtTokenProvider.createToken(
                user.getId(),
                user.getSnsAccountId(),
                user.getSnsType().getId(),
                false
        );
    }
}
