package tf.tailfriend.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tf.tailfriend.notification.entity.UserFcm;
import tf.tailfriend.notification.entity.dto.UserFcmDto;
import tf.tailfriend.notification.repository.UserFcmDao;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserFcmService {

    private final UserFcmDao userFcmDao;

    // FCM 토큰 저장 또는 갱신

    public List<UserFcm> findByUserId(Integer userId) {
        return userFcmDao.findAllByUserId(userId);
    }

    @Transactional
    public void saveOrUpdate(UserFcmDto dto) {
        UserFcm existing = userFcmDao
                .findByUserIdAndMobileAndDev(dto.getUserId(), dto.isMobile(), dto.isDev())
                .orElse(null);

        if (existing != null) {
            UserFcm updated = UserFcm.builder()
                    .id(existing.getId())  // 기존 ID 유지
                    .userId(dto.getUserId())
                    .fcmToken(dto.getFcmToken())
                    .mobile(dto.isMobile())
                    .dev(dto.isDev())
                    .createdAt(existing.getCreatedAt())  // 선택: 생성일 유지
                    .build();
            userFcmDao.save(updated);
        } else {
            UserFcm newFcm = UserFcm.builder()
                    .userId(dto.getUserId())
                    .fcmToken(dto.getFcmToken())
                    .mobile(dto.isMobile())
                    .dev(dto.isDev())
                    .build();
            userFcmDao.save(newFcm);
        }
    }
}