package tf.tailfriend.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tf.tailfriend.notification.entity.UserFcm;
import tf.tailfriend.notification.entity.dto.UserFcmDto;
import tf.tailfriend.notification.repository.UserFcmDao;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserFcmService {

    private final UserFcmDao userFcmDao;

    // FCM 토큰 저장 또는 갱신
    @Transactional
    public void registerOrUpdateFcmToken(UserFcmDto dto) {
        UserFcm userFcm = UserFcm.builder()
                .userId(dto.getUserId())
                .fcmToken(dto.getFcmToken())
                .build();

        userFcmDao.save(userFcm);  // 새로운 FCM 토큰을 저장
    }

    @Transactional(readOnly = true)
    public Optional<String> getFcmTokenByUserId(Integer userId) {
        return userFcmDao.findByUserId(userId)
                .map(UserFcm::getFcmToken);
    }
    @Transactional(readOnly = true)
    public boolean existsByUserId(Integer userId) {
        return userFcmDao.existsByUserId(userId);
    }

}