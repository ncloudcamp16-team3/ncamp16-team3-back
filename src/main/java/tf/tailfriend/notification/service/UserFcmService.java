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

    public Optional<UserFcm> findByUserId(Integer userId) {
        return userFcmDao.findByUserId(userId);
    }

    @Transactional
    public void saveOrUpdate(UserFcmDto dto) {
        Optional<UserFcm> existing = userFcmDao.findByUserId(dto.getUserId());

        if (existing.isPresent()) {
            UserFcm updated = UserFcm.builder()
                    .id(existing.get().getId())  // 기존 ID 유지
                    .userId(dto.getUserId())
                    .fcmToken(dto.getFcmToken())
                    .build();
            userFcmDao.save(updated);
        } else {
            UserFcm newFcm = UserFcm.builder()
                    .userId(dto.getUserId())
                    .fcmToken(dto.getFcmToken())
                    .build();
            userFcmDao.save(newFcm);
        }
    }


}