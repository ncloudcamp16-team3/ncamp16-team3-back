package tf.tailfriend.notification.entity.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFcmDto {

        private Integer userId;
        private String fcmToken;
        private boolean dev;
        private boolean mobile;
}
