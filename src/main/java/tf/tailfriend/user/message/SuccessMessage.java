package tf.tailfriend.user.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuccessMessage {
    USER_INFO_SAVE_SUCCESS("유저정보 저장에 성공하였습니다");

    private final String message;
}
